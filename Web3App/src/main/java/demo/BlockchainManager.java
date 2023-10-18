package demo;

import account.Member;
import org.web3j.generated.contracts.Bank;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.admin.AdminPeers;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.admin.Admin;
import org.web3j.quorum.Quorum;
import org.web3j.tx.ClientTransactionManager;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import account.Account;

public class BlockchainManager {

    private HttpService httpService;
    private EthAccounts ethAccounts;
    private PersonalUnlockAccount deployerAccount;
    private ClientTransactionManager clientTransactionManager;
    private Admin admin;
    private Quorum quorum;
    private HashMap<String, Account> accounts;
    private List<String>enodes;

    private String contractAddress;

    public Bank bank;

    public BlockchainManager() throws Exception {

        this.httpService = new HttpService("http:localhost:22000");
        this.quorum = Quorum.build(httpService);
        this.admin = Admin.build(httpService);


        this.ethAccounts = quorum.ethAccounts().send();
        final String ethFirstAccount = ethAccounts.getAccounts().get(0);

        System.out.println("Admin address: " + ethAccounts.getAccounts());
        this.deployerAccount = admin.personalUnlockAccount(ethFirstAccount, "").send();

        if(!(this.deployerAccount.accountUnlocked())) {
            throw new IllegalStateException("Account " + ethFirstAccount + " cannot be unlocked...");
        }

        this.clientTransactionManager = new ClientTransactionManager(
                quorum,
                ethFirstAccount
        );

        this.accounts = new HashMap<>();
        this.initialiseAccounts();

        File contractAddress = new File("contractaddress.txt");

        if(contractAddress.exists()) {
            this.contractAddress = getContractAddress();
            this.bank = this.loadContract();
        } else {
           this.bank = this.deployContract();
            this.contractAddress = createContractAddress();
        }
        System.out.println("Contract address: " + this.contractAddress);
    }
    public Bank deployContract() throws Exception {

        Bank bank = Bank.deploy(
        quorum,
        clientTransactionManager,
        BigInteger.valueOf(0),
        BigInteger.valueOf(4300000), this.enodes).send();

        return bank;

    }

    public Bank loadContract() {
        return Bank.load(this.contractAddress,
                quorum,
                clientTransactionManager,
                BigInteger.valueOf(0),
                BigInteger.valueOf(4300000));
    }

    public void initialiseAccounts() throws Exception {

        this.enodes = new ArrayList<>();

        String deployerEnode = quorum.adminNodeInfo().send().getResult().getEnode().substring(8, 136);

        List<AdminPeers.Peer> adminPeers = quorum.adminPeers().send().getResult();

        if(adminPeers.size() == 1) {
            this.accounts.putIfAbsent("validator" + (adminPeers.size()), new account.Admin(deployerEnode,
                    "validator" + (adminPeers.size())));
        } else {
            this.accounts.putIfAbsent("validator" + (adminPeers.size()-1), new account.Admin(deployerEnode,
                    "validator" + (adminPeers.size()-1)));
        }

        this.enodes.add(deployerEnode);

        for(int i = 0; i <= adminPeers.size() /2; i++) {
            String memberEnode = adminPeers.get(i).getEnode().substring(8, 136);
            String memberName = "member" + (i + 1);
            this.accounts.putIfAbsent(memberName, new Member(memberEnode, memberName));
            this.enodes.add(memberEnode);

            if(i + (adminPeers.size() /2) + 1 < adminPeers.size()) {
                String adminEnode = adminPeers.get(i + (adminPeers.size() / 2 + 1)).getEnode().substring(8, 136);
                String adminName = "validator" + (i + 1);
                this.accounts.putIfAbsent(adminName, new account.Admin(adminEnode, adminName));
                this.enodes.add(adminEnode);
            }
        }

        //this.accounts.entrySet().forEach(key -> System.out.println(key));
    }

    public HashMap<String,Account> getAccounts() {

        return this.accounts;

    }

    public String getContractAddress() {

        String contrAddr = "";

        try{

            Scanner scanner = new Scanner(Paths.get("contractaddress.txt"));

            contrAddr = scanner.next();

        }catch(IOException e) {
            e.printStackTrace();
        }

        return contrAddr;
    }

    public String createContractAddress() {

        String contrAddr = "";

        try {

            contrAddr = this.bank.getContractAddress();

            File contractAddress = new File("contractaddress.txt");

            contractAddress.createNewFile();
            PrintWriter printWriter = new PrintWriter(contractAddress);
            printWriter.print(bank.getContractAddress());
            printWriter.close();
            return contrAddr;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return contrAddr;
    }
}
