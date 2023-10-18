package account;

public abstract class Account {

    private String name;
    private String enode;

    public Account(String enode, String name) {

        this.enode = enode;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getEnode(){return this.enode;}

    public String getAccountType() {
        if(this instanceof Member) {
            return "Member";
        } else {
            return "Admin";
        }
    }

    public String toString() {

        return this.name + ", " + this.enode + ", " + this.getAccountType();

    }


}
