package com.example.baseblock.blockchain;

import com.example.baseblock.blockchain.contract.TicketNFT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import java.math.BigInteger;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class NftService {

    private final Web3j web3j;

    @Qualifier("web3TransactionManager")         // ✅ 변경된 이름으로 주입
    private final org.web3j.tx.TransactionManager transactionManager;     // RawTransactionManager (Web3Config)

    private final ContractGasProvider gasProvider;  // DefaultGasProvider or EIP-1559 provider

    @Qualifier("ticketContractAddress")
    private final String ticketContractAddress;

    private static final String ERC721_TRANSFER_SIG =
            org.web3j.crypto.Hash.sha3String("Transfer(address,address,uint256)");


    private TicketNFT contract() {
        return TicketNFT.load(
                ticketContractAddress,
                web3j,
                transactionManager,
                gasProvider
        );
    }

    @Value("${custody.address}")
    private String custody;                         // 보관(플랫폼) 지갑 주소

    /** 표준 ERC-721: ownerOf(tokenId) -> address */
    public String ownerOf(BigInteger tokenId) throws Exception {
        Function fn = new Function(
                "ownerOf",
                Collections.singletonList(new Uint256(tokenId)),
                Collections.singletonList(new TypeReference<Address>() {})
        );
        String data = FunctionEncoder.encode(fn);
        EthCall res = web3j.ethCall(
                Transaction.createEthCallTransaction(null, ticketContractAddress, data),
                DefaultBlockParameterName.LATEST
        ).send();

        if (res.isReverted()) {
            throw new IllegalStateException("ownerOf reverted: " + res.getRevertReason());
        }
        List<Type> decoded = FunctionReturnDecoder.decode(res.getValue(), fn.getOutputParameters());
        return ((Address) decoded.get(0)).getValue();
    }

    /** 표준 ERC-721: safeTransferFrom(from, to, tokenId) */
    public String safeTransferFromCustody(String to, BigInteger tokenId) throws Exception {
        Function fn = new Function(
                "safeTransferFrom",
                Arrays.asList(new Address(custody), new Address(to), new Uint256(tokenId)),
                Collections.emptyList()
        );
        String txHash = sendFunction("safeTransferFrom", fn);
        waitReceipt(txHash);
        return txHash;
    }

    // =========================
    // 민팅(자동 tokenId 할당형) -> Transfer 이벤트에서 tokenId 파싱
    // =========================
    /** 컨트랙트가 tokenId를 내부에서 발급하는 형태: mint(address to) */
    public MintResult mintTicket(String to, java.math.BigInteger gameId, String seatNo) throws Exception {
        Function fn = new Function("mintTicket",
                java.util.Arrays.asList(new Address(to), new Uint256(gameId), new Utf8String(seatNo)),
                java.util.Collections.emptyList());
        String txHash = sendFunction("mint", fn);
        TransactionReceipt receipt = waitReceipt(txHash);
        // A-1) 영수증 상태 확인: 실패면 즉시 예외
        String status = receipt.getStatus();
        if (status == null || !"0x1".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Mint tx failed (status=" + status + "): " + txHash);
        }

        // Transfer(address,address,uint256) 이벤트의 indexed tokenId 파싱
        BigInteger tokenId = extractTokenIdFromTransfer(receipt);
        return new MintResult(txHash, tokenId);
    }

    // ============== 내부 공통 ==============

    private BigInteger gasPrice(String fn) {
        try { return gasProvider.getGasPrice(fn); }       // 신버전: 함수명 인자
        catch (Throwable ignore) { return gasProvider.getGasPrice(); } // 구버전: 무인자
    }

    private BigInteger gasLimit(String fn) {
        try { return gasProvider.getGasLimit(fn); }       // 신버전
        catch (Throwable ignore) { return gasProvider.getGasLimit(); } // 구버전
    }

    private String sendFunction(String fnName, Function fn) throws Exception {
        String data = FunctionEncoder.encode(fn);
        EthSendTransaction send = transactionManager.sendTransaction(
                gasPrice(fnName),
                gasLimit(fnName),
                ticketContractAddress,
                data,
                BigInteger.ZERO
        );
        String txHash = send.getTransactionHash();
        if (txHash == null) {
            throw new IllegalStateException(fnName + " failed: " + send.getError().getMessage());
        }
        return txHash;
    }

    private TransactionReceipt waitReceipt(String txHash) throws Exception {
        int tries = 0;
        while (tries++ < 90) { // 최대 ~90초
            EthGetTransactionReceipt r = web3j.ethGetTransactionReceipt(txHash).send();
            if (r.getTransactionReceipt().isPresent()) {
                return r.getTransactionReceipt().get();
            }
            Thread.sleep(1000L);
        }
        throw new IllegalStateException("Receipt timeout: " + txHash);
    }

    /** 첫 번째 Transfer 이벤트의 tokenId를 파싱 (필요 시 고도화 가능) */
    private BigInteger extractTokenIdFromTransfer(TransactionReceipt receipt) {
        for (org.web3j.protocol.core.methods.response.Log l : receipt.getLogs()) {
            java.util.List<String> topics = l.getTopics();
            if (topics == null || topics.size() < 4) continue;
            // B-1) 첫 토픽이 Transfer 이벤트 시그니처인지 확인
            if (!ERC721_TRANSFER_SIG.equalsIgnoreCase(topics.get(0))) continue;
            // B-2) topics[3] = tokenId (indexed uint256)
            String hex = topics.get(3);
            if (hex != null && hex.startsWith("0x") && hex.length() >= 66) {
                return new java.math.BigInteger(hex.substring(2), 16);
            }
        }
        log.warn("No ERC721 Transfer event found. tx={}", receipt.getTransactionHash());
        return null;
    }

    public String getCustodyAddress() { return custody; }

    /** 자동할당 민팅 결과 DTO */
    public record MintResult(String txHash, BigInteger tokenId) {}



}