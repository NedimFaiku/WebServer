import java.net.*;
import java.io.*;
import java.nio.file.Files;

public class WebServer extends Thread{
    
    //Konstruktori i cili incializon serverin
    public WebServer(Socket clientSocket, int requests) throws IOException {
        //Getinputstream lexon requestin qe vjen nga klienti
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        String s;
        //Tenton ta hap file-in log.txt per ta shkruar kerkesen nga klienti
        try (PrintWriter shkruesi = new PrintWriter(new FileWriter("log.txt", true))) {
            //Per qdo kerkes rritet numri i klientit qe eshte qasur
            requests++;
            //Shkruan numrin e klientin perpara qdo kerkese
            shkruesi.write("Klienti: " + requests + "\n");
            //While egzektueohet perderisa ka kerkesa nga klienti dhe i ruan kerkesat ne stringun s
            while ((s = in.readLine()) != null) {
                System.out.println(s);
                //Shkruan kerkesen qe eshte bere ne log.txt
                shkruesi.write(s + "\n");
                //Nese nuk ka kerkesa nderprehet while
                if (s.isEmpty()) {
                    break;
                }
            }
        }
        //GetOutPutStream dergon response te klienti se qfare do ti shfaqet nga response 
        DataOutputStream clientOutput = new DataOutputStream(clientSocket.getOutputStream());
        try {
            //E ruan html file ne variablen file
            File file = new File("index.html");
            FileInputStream fileStream = new FileInputStream(file);

            // Merr llojin e fajllit qe klienti po kerkon
            String contentType = Files.probeContentType(file.toPath());

            // Krijon nje BufferedInputStream qe lexon te dhena nga fileStream
            BufferedInputStream bufInputStream = new BufferedInputStream(fileStream);

            // Krijon nje varg te byteve me gjatsi te njejte te file-it qe kerkohet nga klienti
            byte[] bytes = new byte[(int) file.length()];

            // Dergon header te HTTP me pergjigje 200
            clientOutput.writeBytes("HTTP/1.1 200 OK\r\nContent-Type: " + contentType + "\r\n\r\n");

            //Lexon te dhenat nga fajlli i kerkuar ne vargun e biteve
            bufInputStream.read(bytes);

            //Dergo te dhenat qe permbahen nga vargu i byteve tek klienti dhe i shtyn te dhenat
            clientOutput.write(bytes);
            clientOutput.flush();
            System.err.println("Client connection closed!");
            //Mbyllet kerkesa nga klienti
            in.close();
            
            //Mbyllet pergjigjja nga serveri
            clientOutput.close();
            bufInputStream.close();
            clientOutput.close();
            
        } 
        //Nese fajlli qe tenton te hapim nuk gjindet ne kete rast index.html
        catch (FileNotFoundException e) {
            System.out.println("FileNotFound");
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        //numri i portit qe do te hapet 
        int port = 4500;
        //numri i kerkesave per klient qe fillon nga 1
        int requests = 1;
        while (true) {
            //try per IOException per pranimin e kerkesave
            try {
                
                // try per hapjen e serverit ne portin e caktuar
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    System.out.println("Server is running on port: " + port);
                    
                    //Pranimi i kerkeses nga klienti
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected");
                    
                    //krijimi i nje thread per kete kerkes
                    Thread connectionThread = new Thread((Runnable) new WebServer(clientSocket, requests));
                    //Startimi i kerkeses
                    connectionThread.start();
                    //Rritet per 1 numri i klientit qe ka bere kerkesen
                    requests++;
                    //Mbyllet Socketi per klientin
                    clientSocket.close();
                }

            }
            //IOException nuk funksion Socketi jep error
             catch (IOException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
    }
}