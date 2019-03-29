package ua.procamp.bank;

public class Account {

    private int id;

    private int amount;

    public Account(int id, int amount) {
        this.id = id;
        this.amount = amount;
    }

    public void deposit(int amount) {
        this.amount += amount;
    }

    public void withdraw(int amount) {
        this.amount -= amount;
    }

    public int getAmount() {
        return this.amount;
    }
}
