package demo;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import account.Account;
import javafx.stage.Stage;
import org.web3j.generated.contracts.Bank;

import java.math.BigInteger;

public class Application extends javafx.application.Application {

    private MemberView memberView;
    private StaffView staffView;
    public BlockchainManager blockchainManager;
    private SimpleBooleanProperty isAccountAdmin;
    private Account currentAccount;

    @Override
    public void start(Stage stage) throws Exception {

        this.blockchainManager = new BlockchainManager();

        this.isAccountAdmin = new SimpleBooleanProperty(false);

        this.currentAccount = this.blockchainManager.getAccounts().get("member1");

        FXMLLoader memberLoader = new FXMLLoader(getClass().getClassLoader()
                .getResource("com/example/demo/member-view.fxml"));
        Parent memberRoot = memberLoader.load();
        this.memberView = memberLoader.getController();
        this.memberView.setMainApplication(this);
        this.memberView.setMemberChoice(this.blockchainManager.getAccounts().keySet());

        FXMLLoader staffLoader = new FXMLLoader(getClass().getClassLoader()
                .getResource("com/example/demo/staff-view.fxml"));
        Parent staffRoot = staffLoader.load();
        this.staffView = staffLoader.getController();
        this.staffView.setMainApplication(this);
        this.staffView.setMemberChoice(this.blockchainManager.getAccounts().keySet());

        memberView.setBank(blockchainManager.bank);
        staffView.setBank(blockchainManager.bank);

        Scene memberScene = new Scene(memberRoot);
        Scene staffScene = new Scene(staffRoot);

        this.memberView.setTransactionTable();
        this.memberView.setCurrentMemberId();
        this.memberView.setSelectedMember(1);
        this.memberView.disableCurrentUser();

        stage.setTitle("Blockchain Bank");
        stage.setScene(memberScene);
        stage.setResizable(false);
        stage.show();

        this.isAccountAdmin.addListener((observable, old, newValue) -> {

            if(newValue) {
                memberView.setAccountSelectionValue();
                staffView.setAccountSelectionValue();
                staffView.renderView();
                stage.setScene(staffScene);
            } else {
                memberView.setAccountSelectionValue();
                staffView.setAccountSelectionValue();
                stage.setScene(memberScene);
            }
        });

        //System.out.println(blockchainManager.bank.getBalance(BigInteger.valueOf(1)).send());
    }

    public Account getAccount(String accountName) {
        return this.blockchainManager.getAccounts().get(accountName);
    }

    public void setIsAccountAdmin(boolean isAccountAdmin) {
        this.isAccountAdmin.set(isAccountAdmin);
    }

    public void setCurrentAccount(String accountName) {
        this.currentAccount = this.blockchainManager.getAccounts().get(accountName);
    }

    public Account getCurrentAccount() {
        return this.currentAccount;
    }

    public static void main(String[] args) {
        launch();
    }
}