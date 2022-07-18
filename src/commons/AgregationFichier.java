package commons;

import java.io.*;

public class AgregationFichier {

    public static void main(String[] args) {
        File ficIn = new File(args[0]);
        File ficOut = new File(args[1]);

        try (
                PrintWriter out = new PrintWriter(new FileWriter(ficOut, true));
                BufferedReader in = new BufferedReader(new FileReader(ficIn));
            ) {

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("package ") || line.startsWith("import ")
                        || line.trim().startsWith("logDebug(")
                        || line.trim().startsWith("log(")) {
                    /* Ne pas ecrire la ligne */
                } else {
                    out.println(line);
                }
            }
        
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
