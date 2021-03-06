package pkg1;


import java.io.*;

/**
 * @author William Pfeffer
 *         Created on 5/1/2015
 *         <p>
 *         Description:
 *         <p>
 *         Expected Use
 *         <p>
 *         Limitations:
 */
public class CjsHandler implements ITextExtractionHandler {

    private static CjsHandler instance = null;

    private CjsHandler() {
    }

    public static CjsHandler getInstance() {
        if (instance == null) {
            instance = new CjsHandler();
        }
        return instance;
    }

    @Override
    public void Extract(File from, File to){
        BufferedReader br;
        BufferedWriter bw;
        try {
            br = new BufferedReader(new FileReader(from));
            bw = new BufferedWriter(new FileWriter(to));
            String line;
            while ((line = br.readLine()) != null) {
                String plainText = line.replaceAll("[^\\w]", " ")
                        .replaceAll("\\s+", " ")
                        .toLowerCase();
                bw.write(plainText);
                bw.newLine();
            }
            br.close();
            bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}