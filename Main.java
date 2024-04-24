import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
 

// Main class
public class Main {
 
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        //store number of actions
        int n = Integer.parseInt(scan.nextLine());
        
        for (int i = 0; i < n; i++) {
            // scan current operation
            String [] currentInput = scan.nextLine().split(" ");
            switch (currentInput[0]) {
                case "Create":
                    // create a new account
                    BankingSystemProxy.getInstance().addClient(map(currentInput[2]), 
                        currentInput[3], Double.parseDouble(currentInput[4]),
                            currentInput[2]);
                    break;
 
                case "Deposit":
                    // deposit to the existing account
                    BankingSystemProxy.getInstance().Deposit(currentInput[1], 
                        Double.parseDouble(currentInput[2]));
                    break;
 
                case "Withdraw":
                    // withdrawl from the existins account
                    BankingSystemProxy.getInstance().Withdrawal(currentInput[1],
                        Double.parseDouble(currentInput[2]));
                    break;
 
                case "Transfer":
                    // transfer money from one to another account
                    BankingSystemProxy.getInstance().transfer(currentInput[1], currentInput[2], 
                        Double.parseDouble(currentInput[3]));    
                    break; 
                
                case "View":
                    // view all history from a particular account
                    BankingSystemProxy.getInstance().view(currentInput[1]);
                    break;
                
                case "Activate":
                    // activate account
                    BankingSystemProxy.getInstance().activateAccount(currentInput[1]);
                    break;
 
                case "Deactivate":
                    // deactivate account
                    BankingSystemProxy.getInstance().deactivateAccount(currentInput[1]);
                    break;
            
                default:
                    break;
            }
 
 
        }
 
        scan.close();
    }
    
    // mapping account type with transfer Fee, just for convinient creation;
    public static double map(String accountType) {
        if (accountType.equals("Savings"))
            return 0.985;
        if (accountType.equals("Checking"))
            return 0.98;
        if (accountType.equals("Business"))
            return 0.975;
        return 0;
 
    }
}
 

// Proxy pattern, proxy for main class Banking system
// this class handle all errors and redirect operations to the original banking system
class BankingSystemProxy {
    // instance of realBanking, needed for redirect and checking errors
    private BankingSystem realBankingSystem;
    // instance of this class, Singleton pattern
    // actually here is double singleton, original banking system also contains such implementation
    private static BankingSystemProxy proxy;

    //constructor
    private BankingSystemProxy () {
        this.realBankingSystem = BankingSystem.getInstance();
    }

    //singleton
    public static BankingSystemProxy getInstance() {
        if (proxy == null)
            proxy = new BankingSystemProxy();
        return proxy;
    }
    

    //activate an account
    public void activateAccount (String clientName) {

        if (!realBankingSystem.getClients().containsKey(clientName)) {
            Errors.errorExist(clientName);
            return;
        }
        if (realBankingSystem.getClients().get(clientName).state.isState()) {
            Errors.errorActivated(clientName);
            return;
        }
        
        // after handling erros redirect to the real bankingsystem
        realBankingSystem.activateAccount(clientName);
 
    }
    
    //deactivate an account
    public void deactivateAccount(String clientName) {
        if (!realBankingSystem.getClients().containsKey(clientName)) {
            Errors.errorExist(clientName);
            return;
        }
        if (!realBankingSystem.getClients().get(clientName).state.isState()) {
            Errors.errorDeactivated(clientName);
            return;
        }
        // after handling erros redirect to the real bankingsystem
        realBankingSystem.deactivateAccount(clientName);
    }
    
    //viev history of the account
    public void view(String clientName) {
        if (!realBankingSystem.getClients().containsKey(clientName)) {
            Errors.errorExist(clientName);
            return;
        }
        // after handling erros redirect to the real bankingsystem
        realBankingSystem.view(clientName);
    }
 
    // create an account, and add him to the bank data
    public void addClient (double transferFee, String name, double deposit, String accountType){
        if(realBankingSystem.getClients().containsKey(name)) {
            System.out.println("Error: Account " + name + " already exists.");
            return;
        }
        // after handling erros redirect to the real bankingsystem
        realBankingSystem.addClient(transferFee, name, deposit, accountType);
    }
 
    // withdrawal cash
    public void Withdrawal(String accountName, double withdrawal) {
        if (!realBankingSystem.getClients().containsKey(accountName)) {
            Errors.errorExist(accountName);
            return;
        }
 
        if (!realBankingSystem.getClients().get(accountName).state.isState()) {
            Errors.errorInactive(accountName);
            return;
        }
 
        if (realBankingSystem.getClients().get(accountName).getDeposit() < withdrawal) {
            Errors.errorFunds(accountName);
            return;
        }
 
        // after handling erros redirect to the real bankingsystem
        realBankingSystem.Withdrawal(accountName, withdrawal);
    }
 
    // deposit to the account
    public void Deposit(String accountName, double deposit) {
        if (!realBankingSystem.getClients().containsKey(accountName)) {
            Errors.errorExist(accountName);
            return;
        }
        // after handling erros redirect to the real bankingsystem
        realBankingSystem.Deposit(accountName, deposit);
 
    }

    //transfer money from one to another account
    public void transfer(String firstAccountName, String secondAccountName, double transfer) {
        if (!realBankingSystem.getClients().containsKey(firstAccountName)) {
            Errors.errorExist(firstAccountName);
            return;
        }
 
        if (!realBankingSystem.getClients().containsKey(secondAccountName)) {
            Errors.errorExist(secondAccountName);
            return;
        }

        if (!realBankingSystem.getClients().get(firstAccountName).state.isState()) {
            Errors.errorInactive(firstAccountName);
            return;
        }
 
        if (realBankingSystem.getClients().get(firstAccountName).getDeposit() < transfer) {
            Errors.errorFunds(firstAccountName);
            return;
        }
        
        // after handling erros redirect to the real bankingsystem
        realBankingSystem.transfer(firstAccountName, secondAccountName, transfer);
    }
 
}
 
// real banking system functionality
class BankingSystem {
    // itself instance - singleton pattern
    private static BankingSystem instance;
    
    // data base of clients
    private HashMap<String, Account> clients;
    
    public HashMap<String, Account> getClients() {
        return clients;
    }

    private BankingSystem(){
        clients = new HashMap<>();
    }

    //singleton
    public static BankingSystem getInstance() {
        if (instance == null) 
            instance = new BankingSystem();
        return instance;
    }
 
    // activate an account
    public void activateAccount(String clientName) {
        
        clients.get(clientName).state = new ActivatedState();
        System.out.println(clientName + "'s account is now activated.");
 
    }
    
    // deactivate an account
    public void deactivateAccount(String clientName) {
 
        clients.get(clientName).state = new DeactivatedState();
        System.out.println(clientName + "'s account is now deactivated.");
 
    }
 
    // viev account history
    public void view(String clientName) {
        
        clients.get(clientName).viewHistory();
 
    }
    
    // create ann account and add him to data base
    public void addClient (double transferFee, String name, double deposit, String accountType) {
 
        clients.put(name, new Account(transferFee, name, deposit, accountType));
        System.out.println("A new " + accountType + " account created for " + name + " with an initial balance of $" + String.format("%.3f", deposit) + ".");
            
 
    } 
    
    // withdrawal from an account
    public void Withdrawal(String accountName, double withdrawal) {
 
        clients.get(accountName).addActionToTheHistory(ForHistory.Withdrawal(withdrawal));
        clients.get(accountName).setDeposit(clients.get(accountName).getDeposit() - withdrawal);
        System.out.println(accountName + " successfully withdrew $" 
            + String.format("%.3f", (withdrawal * clients.get(accountName).getTransferFee()))
                + ". New Balance: $" + String.format("%.3f" ,clients.get(accountName).getDeposit()) +
                     ". Transaction Fee: $" + String.format("%.3f", (withdrawal - withdrawal * clients.get(accountName).getTransferFee()))
                      + " (" + (String.format("%.1f", (1 - clients.get(accountName).getTransferFee()) *100))+"%) in the system.");
 
    }
    
    // deposit to an account
    public void Deposit(String accountName, double deposit) {
      
        clients.get(accountName).addActionToTheHistory(ForHistory.Deposit(deposit));
        clients.get(accountName).setDeposit(clients.get(accountName).getDeposit() + deposit);
        System.out.println(accountName + " successfully deposited $" + String.format("%.3f",deposit) 
            + ". New Balance: $" + String.format("%.3f",clients.get(accountName).getDeposit()) + ".");
 
    }
 
    // transfer money from one to anoter account
    public void transfer(String firstAccountName, String secondAccountName, double transfer) {
       
        Account firstAccount = clients.get(firstAccountName);
        Account secondAccount = clients.get(secondAccountName);
 
        firstAccount.setDeposit(firstAccount.getDeposit() - transfer);
        secondAccount.setDeposit(secondAccount.getDeposit() + transfer * firstAccount.getTransferFee());
 
        firstAccount.addActionToTheHistory(ForHistory.Transfer(transfer));
 
        System.out.println(firstAccountName + " successfully transferred $" + String.format("%.3f" , transfer * firstAccount.getTransferFee()) + " to " 
            + secondAccountName + ". New Balance: $" + String.format("%.3f", firstAccount.getDeposit()) + "."
                + " Transaction Fee: $" + String.format("%.3f",(transfer - transfer * firstAccount.getTransferFee())) 
                    + " (" + String.format("%.1f",((1 - firstAccount.getTransferFee()) * 100)) + "%) in the system.");
    }
 
} 
 
// states for account class
class AccountState {
    public boolean state;
    public String stringState;
 
    public boolean isState() {
        return state;
    }  
 
}
 // activated state
class ActivatedState extends AccountState {
 
    public ActivatedState() {
        this.state = true;
        this.stringState = "Active";
    }
    
    
}
 // deactivated state
class DeactivatedState extends AccountState {
 
    public DeactivatedState() {
        this.state = false;
        this.stringState = "Inactive";
    }
    
}
 
// class that represent an account in banking system
class Account {
 
    private double transferFee;
 
    // contains state as classes represent state design pattern
    public AccountState state;
 
    private double deposit;
 
    private String ownerName;
 
    private ArrayList <String> operationHistory;
 
    private String accountType;
 
    // add operation to the user history
    public void addActionToTheHistory(String s) {
        operationHistory.add(s);
    }

 
    public double getTransferFee() {
        return transferFee;
    }


    public double getDeposit() {
        return deposit;
    }
 
 
    public void setDeposit(double deposit) {
        this.deposit = deposit;
    }
 
    // view user history
    public void viewHistory () {
        System.out.print(ownerName + "'s Account: Type: " + accountType + ", Balance: $" + String.format("%.3f", deposit) +
             ", State: " + state.stringState + ", Transactions: [");
        if (!operationHistory.isEmpty())
            System.out.print(operationHistory.get(0));
        for(int i = 1; i < operationHistory.size(); i++) {
            System.out.print(", " + operationHistory.get(i));
        }
        System.out.print("].");
        System.out.println();
    }
 
    // constructor for creating account
    public Account(double transferFee, String name, double deposit, String accountType) {
        this.transferFee = transferFee;
        this.state = new ActivatedState();
        this.ownerName = name;
        this.deposit = deposit;
        this.operationHistory = new ArrayList<>();
        this.operationHistory.add(ForHistory.InitialDeposit(deposit));
        this.accountType = accountType;
    }
 
    
}


// class for Errors, just for simplify code readability
class Errors {
    public static void errorExist (String x) {
        System.out.println("Error: Account " + x +  " does not exist.");
    }
 
    public static void errorInactive (String x) {
        System.out.println("Error: Account " + x +  " is inactive.");
    }
 
    public static void errorFunds (String x) {
        System.out.println("Error: Insufficient funds for " + x + ".");
    }
 
    public static void errorActivated(String x) {
        System.out.println("Error: Account " + x +  " is already activated.");
    }
 
    public static void errorDeactivated(String x) {
        System.out.println("Error: Account " + x +  " is already deactivated.");
    }
 
}
 
// class for user operations, just for simplify code readability
class ForHistory {
 
    public static String InitialDeposit (double x) {
        return "Initial Deposit $" + String.format("%.3f", x);
    }
 
    public static String Deposit (double x) {
        return "Deposit $" + String.format("%.3f",x);
    }
 
    public static String Withdrawal (double x) {
        return "Withdrawal $" + String.format("%.3f", x);
    }
 
    public static String Transfer (double x) {
        return "Transfer $" + String.format("%.3f", x);
    }
 
}