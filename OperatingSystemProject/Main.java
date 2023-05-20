import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class PhoneStation {
    //Lock nesneleri oluşturdum.
    private Lock lady1 = new ReentrantLock();
    private Lock lady2 = new ReentrantLock();
    private Lock line1 = new ReentrantLock();
    private Lock line2 = new ReentrantLock();
    public int callsCompleted = 0;

    // Arkadaşın aramasını yapmak için bu metot kullanılır.
    // Eğer Bayan 1 ve Hat 1 kullanılabilir durumdaysa, arama gerçekleştirilir.
    public void callFriend(int friend) {
        if (lady1.tryLock() && line1.tryLock()) {
            makeCall(friend, "Line 1", "Lady 1");
        } else if (lady2.tryLock() && line2.tryLock()) {
            makeCall(friend, "Line 2", "Lady 2");
        } else {
            waitAndCall(friend);
        }
    }

   // Arama gerçekleştirme işlemini simüle etmek için kullanılır.
    private void makeCall(int friend, String line, String lady) {
        System.out.println("Friend " + friend + " is talking on " + line + " with " + lady);
        try {
            Thread.sleep(1000); // Simulating the duration of the phone call.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Friend " + friend + " has finished the call");
        callsCompleted++;
        releaseLine(line, lady);
    }


    //Kullanılan hat ve bayan kilidi serbest bırakılır
    private void releaseLine(String line, String lady) {
        if (line.equals("Line 1")) {
            line1.unlock();
            lady1.unlock();
        } else {
            line2.unlock();
            lady2.unlock();
        }
    }

    private void waitAndCall(int friend) { //wait operation opject.
        while (true) {
            if (lady1.tryLock()) {
                if (line1.tryLock()) {
                    makeCall(friend, "Line 1", "Lady 1");
                    break;
                } else {
                    lady1.unlock();
                }
            } else if (lady2.tryLock()) {
                if (line2.tryLock()) {
                    makeCall(friend, "Line 2", "Lady 2");
                    break;
                } else {
                    lady2.unlock();
                }
            } else {
                try {
                    Thread.sleep(1000); // Waiting for a lady to become available.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        PhoneStation station = new PhoneStation();

        // Simulating the phone calls from side A to side B.
        Thread[] threads = new Thread[36];
        for (int friend = 1; friend <= 6; friend++) {
            for (int i = 0; i < 6; i++) {
                int finalFriend = friend;
                Thread thread = new Thread(() -> station.callFriend(finalFriend));
                threads[(friend - 1) * 6 + i] = thread;
                thread.start();
            }
        }

        // Wait for all threads to complete.
        for (Thread thread : threads) {
            thread.join();
        }

        // Checking if all calls are completed.
        if (station.callsCompleted == 36) {
            System.out.println("All phone calls have been completed.");
        } else {
            System.out.println("Some phone calls were not completed.");
        }

        System.out.println("-----------------The Program is Finished----------------");
    }
}
