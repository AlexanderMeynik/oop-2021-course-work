
package model;

public class Crane extends Thread {
    private Ship ship;
    private boolean runFlag;
    private final Object lock = new Object();

    public Crane() {
        ship = null;
        runFlag = true;
        setPriority(MAX_PRIORITY);
        start();
    }

    public void startWork() {
        synchronized (lock) {
            lock.notify();
        }
    }

    @Override
    public void run() {
        super.run();
        while (runFlag) {
            if (ship != null) {
                if (ship.getAmmountOfGoods().updateAndGet(n -> (n > 0) ? n - 1 : n) == 0 && ship.getDelay().get() == 0) {
                    setShip(null);
                }
            }
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isEnded() {
        return ship == null;
    }

    /*private void work() {
        if (ship != null) {
            if (ship.getAmmountOfGoods().updateAndGet(n -> (n > 0) ? n - 1 : n) == 0 && ship.getDelay().get() == 0) {
                setShip(null);
            }
        }
    }*/

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public void terminate() {
        runFlag = false;
        startWork();
    }


}
