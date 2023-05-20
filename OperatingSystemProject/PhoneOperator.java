class Caller implements Runnable {
    private final String callerName;
    private final TelephoneExchange exchange;

    public Caller(String callerName, TelephoneExchange exchange) {//değişen Class ismi
        this.callerName = callerName;
        this.exchange = exchange;
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= 6; i++) {
                String receiver = "Location_B" + i;
                String operator = exchange.connectCall(callerName, receiver);
                System.out.println(callerName + " is calling " + receiver + " (Operator: " + operator + ")");//değişen kısım
                Thread.sleep(1000); // Simulate conversation time
                exchange.endCall(callerName);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class TelephoneExchange {
    private final Object lock = new Object();
    private boolean isLineBusy;
    private int completedCalls;
    private int operatorIndex;

    public TelephoneExchange() {
        this.isLineBusy = false;
        this.completedCalls = 0;
        this.operatorIndex = 0;
    }

    public String connectCall(String caller, String receiver) throws InterruptedException {
        String operator = "Operator " + (operatorIndex + 1);
        synchronized (lock) {
            while (isLineBusy) {
                lock.wait();
            }
            isLineBusy = true;
            System.out.println(caller + " is connected to " + operator);
        }
        Thread.sleep(1000); // Simulate operator connection time
        operatorIndex = (operatorIndex + 1) % 2; // Alternate between two operators
        return operator;
    }

    public void endCall(String caller) {
        synchronized (lock) {
            System.out.println(caller + " completed the call.");
            completedCalls++;
            isLineBusy = false;
            if (completedCalls == 36) {
                System.out.println("All calls completed");
            }
            lock.notify();
        }
    }

    public int getCompletedCalls() {
        return completedCalls;
    }
}

class Telephone_Simulation {
    public static void main(String[] args) throws InterruptedException {
        TelephoneExchange exchange = new TelephoneExchange();

        Thread[] callers = new Thread[6];
        for (int i = 0; i < 6; i++) {
            Caller caller = new Caller("Location_A" + (i + 1), exchange);
            callers[i] = new Thread(caller);
        }

        Thread operatorThread = new Thread(() -> {
            while (exchange.getCompletedCalls() < 36) {
                synchronized (exchange) {
                    try {
                        exchange.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        });

        operatorThread.start();

        for (Thread caller : callers) {
            caller.start();
        }

        for (Thread caller : callers) {
            caller.join();
        }

        synchronized (exchange) {
            exchange.notify();
        }

        operatorThread.join();

    }
}

