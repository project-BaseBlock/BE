package com.example.baseblock.blockchain;

import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.RawTransactionManager;

@Configuration
public class Web3Config {

    @Value("${chain.rpc-url}")
    private String rpcUrl;

    @Value("${custody.private-key}")
    private String custodyPrivateKey;

    @Bean
    public Web3j web3j() {
        if (rpcUrl == null || rpcUrl.isBlank())
            throw new IllegalStateException("필수 설정 누락: chain.rpc-url (env: CHAIN_RPC_URL)");
        return Web3j.build(new HttpService(rpcUrl));
    }

    @Bean
    public Credentials custodyCredentials() {
        if (custodyPrivateKey == null || custodyPrivateKey.isBlank())
            throw new IllegalStateException("필수 설정 누락: custody.private-key (env: MINTER_PK)");
        if (!custodyPrivateKey.startsWith("0x") || custodyPrivateKey.length() != 66)
            throw new IllegalArgumentException("custody.private-key 형식 오류: 0x + 64 hex 필요");
        return Credentials.create(custodyPrivateKey);
    }

    @Bean
    public ContractGasProvider contractGasProvider() {
        return new DefaultGasProvider(); // 필요시 EIP-1559로 커스텀
    }

    @Bean(name = "ticketContractAddress")
    public String ticketContractAddress(@Value("${ticket.nft-address}") String addr) {
        if (!addr.startsWith("0x") || addr.length() != 42) {
            throw new IllegalArgumentException("ticket.contract-address 형식 오류: 0x + 40 hex 필요");
        }
        return addr;
    }

    @Bean(name = "web3TransactionManager")
    public TransactionManager transactionManager(
            Web3j web3j,
            Credentials custodyCredentials,
            @Value("${chain.id:11155111}") long chainId
    ) {
        // 가장 간단한 기본 매니저 (EIP-155 체인ID 포함)
        return new RawTransactionManager(web3j, custodyCredentials, chainId);

        // 네트워크가 느리면 FastRawTransactionManager + 커스텀 receipt processor도 가능:
        // var receiptProcessor = new PollingTransactionReceiptProcessor(web3j, 1000L, 15);
        // return new FastRawTransactionManager(web3j, custodyCredentials, chainId, receiptProcessor);
    }

    @PostConstruct
    public void logAddrs() {
        String shortRpc = (rpcUrl == null) ? "null" : rpcUrl.replaceAll("^https?://", "").split("\\?")[0];
        String shortCust = (custodyPrivateKey == null) ? "null" : custodyPrivateKey.substring(0, 6) + "...";
        LoggerFactory.getLogger(getClass()).info("[Web3] rpc={}, custodyKey={}, ticketAddr(bean at use-time)", shortRpc, shortCust);
    }

}