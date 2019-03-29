package ua.procamp.bank;

import lombok.SneakyThrows;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;

public class MyBank {

    List<Account> accounts;

    public MyBank(List<Account> accounts) {
        this.accounts = accounts;
    }

    public void transfer(int fromAccountId, int toAccountId, int amount) {
        Account fromAccount = accounts.get(fromAccountId);
        Account toAccount = accounts.get(toAccountId);
        synchronized (fromAccount) {
            fromAccount.withdraw(amount);
            synchronized (toAccount) {
                toAccount.deposit(amount);
            }
        }
    }

    public int total() {
        return accounts.stream().mapToInt(Account::getAmount).sum();
    }

    @SneakyThrows
    public static void main(String[] args) {
        MyBank myBank = new MyBank(asList(new Account(1, 100), new Account(2, 100)));

        ExecutorService executorService = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 1000; i++) {
            executorService.execute(() -> {
                myBank.transfer(0, 1, ThreadLocalRandom.current().nextInt(1000));
                myBank.transfer(1, 0, ThreadLocalRandom.current().nextInt(1000));
            });
        }

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            executorService.awaitTermination(1, TimeUnit.SECONDS);
        }

        System.out.println(myBank.total());
    }
}
