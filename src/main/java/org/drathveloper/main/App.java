package org.drathveloper.main;

public class App 
{
    public static void main( String[] args )
    {
        Thread t = new Thread(new TinderBot4J());
        t.start();
        synchronized (t){
            try {
                t.wait();
            } catch(InterruptedException ex){
                System.exit(-1);
            }
        }
    }
}
