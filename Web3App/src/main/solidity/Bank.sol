// SPDX-License-Identifier: UNLICENSED
pragma solidity 0.8.19;

import "./Strings.sol";


contract Bank {

    struct account {
        string addr;
        string name;
        uint balance;
        uint loanBalance;
    }

    struct admin {
        string addr;
        string name;
    }

    struct loanRequest {
        account member;
        uint amount;
    }

    struct transaction {

        bytes32 transactionId;
        string fromAccount;
        string toAccount;
        uint date;
        uint amount;
    }

    mapping(bytes32=>transaction[]) transactions;

    account[] private accounts;
    admin[] private admins;
    loanRequest[] private loanRequests;
    transaction[] private transactionArray;


    constructor(string[] memory enodes) {


        for(uint i = 0; i < enodes.length /2; i++) {

            string memory role = string.concat("member", Strings.toString(i+1));
            accounts.push(account(enodes[i], role, 1000, 0));

        }

        for(uint j = enodes.length /2; j < enodes.length; j++) {

            string memory role = string.concat("validator", Strings.toString((j - (enodes.length /2)+1)));
            admins.push(admin(enodes[j], role));

        }

    }

    function getBalance(uint _memberId) public view returns (uint) {

        return accounts[_memberId-1].balance;

    }

    function getAccounts() public view returns (account[] memory) {

        return accounts;

    }

    function getTransactionArray() public view returns (transaction[] memory) {

        return transactionArray;

    }

    function getTransaction(uint _memberId) public view returns (transaction[] memory) {

        return transactions[keccak256(abi.encodePacked(accounts[_memberId-1].addr))];

    }

    function getMemberName(uint _memberId) public view returns (string memory) {

        return accounts[_memberId-1].name;

    }


    function getLoanOwed(uint _memberId) public view returns (uint) {

        return accounts[_memberId-1].loanBalance;

    }

    function getLoanRequests() public view returns(loanRequest[] memory) {

        return loanRequests;

    }


    function transfer (uint _senderId, uint _recipientId, uint _money) public {

        account memory sender = accounts[_senderId-1];
        account memory recipient = accounts[_recipientId-1];

        require(sender.balance >= _money);
        sender.balance -= _money;
        recipient.balance += _money;

        accounts[_senderId-1] = sender;
        accounts[_recipientId-1] = recipient;

        uint time = block.timestamp;

        transaction memory newTransaction = transaction(keccak256(abi.encodePacked(time, _senderId-1)), sender.name,
            recipient.name, time, _money);

        transactions[keccak256(abi.encodePacked(sender.addr))].push(newTransaction);

        transactions[keccak256(abi.encodePacked(recipient.addr))].push(newTransaction);

        transactionArray.push(newTransaction);


    }


    // _loanee parameter should correspond to number from member's name e.g., member1 = 1.

    function payLoan(uint _loanee, uint _amount) public {

        uint currentLoan = accounts[_loanee-1].loanBalance;

        require(_amount > 0 && _amount <= (accounts[_loanee -1]).balance && _amount <= currentLoan);

        uint newLoan = currentLoan - _amount;

        accounts[_loanee-1].loanBalance = newLoan;
        accounts[_loanee-1].balance -= _amount;

        uint time = block.timestamp;

        transaction memory newTransaction = transaction(keccak256(abi.encodePacked(time, accounts[_loanee-1].name)),
            accounts[_loanee-1].name, "Loan", time, _amount);

        transactionArray.push(newTransaction);
        transactions[keccak256(abi.encodePacked(accounts[_loanee-1].addr))].push(newTransaction);

    }


    function requestLoan(uint _loanee, uint _amount) public {

        require(_amount > 0);

        loanRequests.push(loanRequest(accounts[_loanee-1], _amount));

    }

    // _loanReq represents the loanRequests index from the array.
    function loanResponse(uint _loanReq, bool _choice) public {

        if (_choice == true) {
            account memory loanee = loanRequests[_loanReq].member;

            loanee.loanBalance += loanRequests[_loanReq].amount;
            loanee.balance += loanRequests[_loanReq].amount;

            uint time = block.timestamp;

            transaction memory newTransaction = transaction(keccak256(abi.encodePacked(time, "Loan")), "Loan",
                loanee.name, time, loanRequests[_loanReq].amount);

            transactionArray.push(newTransaction);
            transactions[keccak256(abi.encodePacked(loanee.addr))].push(newTransaction);

            for (uint i = 0; i < accounts.length; i++) {
                if (keccak256(abi.encodePacked(loanRequests[_loanReq].member.name)) ==
                    keccak256(abi.encodePacked(accounts[i].name))) {
                    accounts[i] = loanee;
                }
            }

            for (uint i = _loanReq; i < loanRequests.length - 1; i++) {
                loanRequests[i] = loanRequests[i + 1];
            }
            loanRequests.pop();
        } else {
            for (uint i = _loanReq; i < loanRequests.length - 1; i++) {
                loanRequests[i] = loanRequests[i + 1];
            }
            loanRequests.pop();
        }

    }

}