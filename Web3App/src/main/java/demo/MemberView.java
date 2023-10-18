package demo;

import account.Account;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.generated.contracts.Bank;
import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

public class MemberView implements Initializable {

    private Application mainApplication;
    private Bank bank;
    private int currentMemberId;
    private SimpleIntegerProperty selectedMember;
    @FXML
    private Text selectedMemberName;
    @FXML
    private Text selectedMemberAddr;
    @FXML
    private Text addressText;
    @FXML
    private VBox member2Container;
    @FXML
    private ComboBox accountSelection;
    @FXML
    private Button payLoanButton;
    @FXML
    private Button requestLoanButton;
    @FXML
    private TextArea consoleOutput;
    @FXML
    private Text currentLoanText;
    @FXML
    private Text accountBalance;
    @FXML
    private Button payMemberButton;
    @FXML
    private TextField payMemberText;
    @FXML
    private TableView transactionTable;
    @FXML
    private TextField loanRequestText;
    @FXML
    private Polygon leftArrow;
    @FXML
    private Polygon rightArrow;
    @FXML
    private TableColumn<Bank.transaction, String>transactionId;
    @FXML
    private TableColumn<Bank.transaction, String>transactionFrom;
    @FXML
    private TableColumn<Bank.transaction, String>transactionTo;
    @FXML
    private TableColumn<Bank.transaction, String>transactionDate;
    @FXML
    private TableColumn<Bank.transaction, String>transactionAmount;


    public void setMainApplication(Application mainApplication) {
        this.mainApplication = mainApplication;
        this.setAddressText(this.mainApplication.getAccount("member1").getEnode());
        this.setCurrentMemberId();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        this.selectedMember = new SimpleIntegerProperty(1);

        this.leftArrow.setOnMouseClicked((event) -> {

            int memberNumber = this.mainApplication.blockchainManager.getAccounts().size() /2; // 1 with 2-node config
            //System.out.println(memberNumber);

            if(this.selectedMember.get() == 1) {
                this.selectedMember.set(memberNumber);
            } else {
                this.selectedMember.set(this.selectedMember.get() - 1);
            }

            this.setSelectedMember(this.selectedMember.get());

        });

        this.rightArrow.setOnMouseClicked((event)-> {

            int memberNumber = this.mainApplication.blockchainManager.getAccounts().size() /2;

            if(this.selectedMember.get() == memberNumber) {
                this.selectedMember.set(1);
            } else {
                this.selectedMember.set(this.selectedMember.get() + 1);
            }

            this.setSelectedMember(this.selectedMember.get());
        });

        this.selectedMember.addListener((observable, old, newValue) -> {
            this.setSelectedMember(newValue);
        });

        this.accountSelection.valueProperty().addListener((observable, old, newValue) -> {
            Account newCurrentAccount = this.mainApplication.getAccount(newValue.toString());
            this.setAddressText(newCurrentAccount.getEnode());
            this.mainApplication.setCurrentAccount(newValue.toString());
            if(newCurrentAccount.getAccountType().equals("Admin")) {
                this.mainApplication.setIsAccountAdmin(true);
            } else {
                this.clearConsole();
                this.setCurrentMemberId();
                this.disableCurrentUser();
                this.renderView();
                this.mainApplication.setIsAccountAdmin(false);

            }
        });

        this.accountBalance.textProperty().addListener((observable, old, newValue) -> {
            this.accountBalance.setText(newValue);
        });

        this.payMemberButton.setOnAction((ActionEvent e) -> {
            this.pay(this.selectedMemberName.getText(), this.payMemberText
                    .getText());
            this.renderView();
        });


        this.requestLoanButton.setOnAction((ActionEvent e) -> {

            try{
                this.bank.requestLoan(BigInteger.valueOf(this.currentMemberId), BigInteger
                        .valueOf(Long.valueOf(this.loanRequestText.getText()))).send();
            }catch (Exception ex) {
                ex.printStackTrace();
            }
            this.setConsoleOutput("Successfully sent loan request.");
        });

        this.payLoanButton.setOnAction(event -> {

            try {
                this.bank.payLoan(BigInteger.valueOf(this.currentMemberId), BigInteger
                        .valueOf(Long.valueOf(this.loanRequestText.getText()))).send();
                this.renderView();
            }catch (Exception e) {
                e.printStackTrace();
            }
            this.setConsoleOutput("Loan payment received.");

        });

    }

    public void setSelectedMember(Number index) {

        String key = "member"+index;

        this.selectedMemberName.setText(key);
        this.selectedMemberAddr.setText(this.mainApplication.getAccount(key).getEnode());
        this.disableCurrentUser();
    }

    public void setBank(Bank bank) {

        this.bank = bank;
        this.setAccountBalance();
    }

    public void setCurrentMemberId() {

        String currentUser = this.mainApplication.getCurrentAccount().getName();
        String[] userPieces = currentUser.split("r");
        this.currentMemberId = Integer.valueOf(userPieces[1]);

    }

    public void disableCurrentUser() {

        String member = this.selectedMemberName.getText().toString();

        String[] selectedMember = member.split("r");

        int selectedMemberId = Integer.valueOf(selectedMember[1].trim());

        if(this.currentMemberId == selectedMemberId) {
            this.member2Container.setDisable(true);
        } else {
            this.member2Container.setDisable(false);
        }

    }

    public BigInteger getBalance(String memberName) throws Exception {

        // call with account address in arguments. Requires that it either be an admin or the account.

        String[]member = memberName.split("r");
        int memberNumber = Integer.valueOf(member[1]);

        return this.bank.getBalance(BigInteger.valueOf(memberNumber)).send();
    }

    public void setAccountBalance() {

        try{
            this.accountBalance.setText("Current Balance: \u00a3" + getBalance(this.mainApplication
                    .getCurrentAccount()
                    .getName())
                    .toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pay(String toUser, String amount) {

        String[]to = toUser.split("r");

        int senderId = this.currentMemberId;
        int recipientId = Integer.valueOf(to[1].trim());

        try{
            this.bank.transfer(BigInteger.valueOf(senderId), BigInteger.valueOf(recipientId),
                    BigInteger.valueOf(Integer.valueOf(amount))).send();

            this.setConsoleOutput("Successfully sent \u00a3" + amount + " to " + "member" + recipientId);

        }catch(Exception e) {
            e.printStackTrace();
        }

        this.setAccountBalance();
        this.setTransactionTable();

    }

    public void clearConsole() {
        this.consoleOutput.setText("");
    }

    public void setConsoleOutput(String message) {

        //this.consoleText.append(message + "\n");
        this.consoleOutput.appendText(message + "\n");
    }

    public List getTransaction() {

        List userTransactions = null;

        try{
            userTransactions = this.bank
                    .getTransaction(BigInteger.valueOf(this.currentMemberId))
                    .send();
        }catch(Exception e) {
            e.printStackTrace();
        }

        return userTransactions;
    }

    public void setTransactionTable() {

        ObservableList observableTransactions = FXCollections
                .observableArrayList(this.getTransaction());

        this.transactionTable.setItems(observableTransactions);

        this.transactionId.setCellValueFactory(data -> {
            return new SimpleStringProperty((TypeEncoder.encode(new Bytes32(data.getValue().transactionId))));
        });

        this.transactionFrom.setCellValueFactory(data -> {
            return new SimpleStringProperty(data.getValue().fromAccount);
        });

        this.transactionTo.setCellValueFactory(data -> {
            return new SimpleStringProperty(data.getValue().toAccount);
        });

        ZoneId zoneId = ZoneId.of("Europe/London");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        this.transactionDate.setCellValueFactory(data -> {
            System.out.println(data.getValue().date.longValue());
            Instant instant = Instant.ofEpochSecond(data.getValue().date.longValue());
            ZonedDateTime zonedDateTime = instant.atZone(zoneId);

            return new SimpleStringProperty(formatter.format(zonedDateTime));
        });

        this.transactionAmount.setCellValueFactory(data -> {
            return new SimpleStringProperty("\u00a3" + data.getValue().amount.toString());
        });

    }

    public void setCurrentLoanText() {

        try{
            this.currentLoanText.setText("Current loan due: \u00a3"+this.bank.getLoanOwed(BigInteger
                    .valueOf(this.currentMemberId)).send().toString());
        }catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setMemberChoice(Set<String> nodes) {

        nodes.forEach(key -> this.accountSelection.getItems().addAll(key));

    }

    public void renderView() {
        this.setTransactionTable();
        this.setAccountBalance();
        this.setCurrentLoanText();
    }

    public void setAccountSelectionValue() {
        this.accountSelection.setValue(this.mainApplication.getCurrentAccount().getName());
    }

    public void setAddressText(String address) {
        this.addressText.setText("Address: " +address);
    }
}