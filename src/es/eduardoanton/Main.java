package es.eduardoanton;

import oracle.jrockit.jfr.JFR;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.PDFTextStripper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        JFrame frame =new JFrame("SeleccionFichero");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try{
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("*.PDF","pdf"));
            frame.add(fileChooser);
            frame.setSize(500, 300);
            int seleccion=fileChooser.showOpenDialog(frame);
            System.out.println(fileChooser.getSelectedFile());

            //frame.setVisible(true);
            // Primero procesamos el PDF para extraer los codigos de expediente
            List<String> resultado;
            resultado = procesaPDF(fileChooser.getSelectedFile().toString());
            System.out.println(resultado);

            //Ahora vamos a crear el documento
            PDDocument document = new PDDocument();
            int i=0;
            PDPage pagina = creaPagina(document);
            float altura = pagina.getMediaBox().getHeight();
            float anchura = pagina.getMediaBox().getWidth();
            float posx = 30;
            float posy = altura - 40;
            PDPageContentStream contentStream = new PDPageContentStream(document, pagina,true,false);
            PDFont font = PDType1Font.HELVETICA_BOLD;

            for(String expediente: resultado){
                contentStream.beginText();
                contentStream.setFont(font, 17);
                contentStream.moveTextPositionByAmount(posx, posy);
                System.out.print(expediente);
                contentStream.drawString(expediente);
                contentStream.endText();
                i++;
                if ( i < 20){
                    posy -= 19;
                }else if (i == 20){
                    //Primer cuadro terminado Pasamos al segundo
                    posy = altura/2.0f - 40;
                }else if ( i > 20 && i < 40) {
                    posy -= 19;
                }else if ( i == 40){
                    // Primera columna terminada pasamos a la segunda columna
                    posx = anchura/2.0f + 30;
                    posy =  altura - 40;
                }else if (i > 40 && i < 60){
                    posy -= 19;
                }else if (i == 60){
                    // Tercer cuadro
                    posy = altura/2.0f - 40;
                }else if (i > 60 && i < 80){
                    posy -= 19;
                }else if (i == 80){
                    // Hemos completado una de las páginas así que la grabamos y creamos una plantilla de pagina en blanco nueva
                    i = 0;
                    posx = 30;
                    posy = altura - 40;
                    contentStream.close();
                    document.addPage(pagina);
                    pagina = creaPagina(document);
                    contentStream = new PDPageContentStream(document, pagina,true,false);
                }
            }
            contentStream.close();

            document.addPage( pagina );
            document.save("Etiquetas.pdf");
            document.close();
            if (Desktop.isDesktopSupported()){
                Desktop.getDesktop().open(new File("Etiquetas.pdf"));
            }
            frame.dispose();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            frame.dispose();
        }
    }

    // Procesa el PDF de Imprimir Etiquetas
    public static List<String> procesaPDF(String ruta){
        try {
            List<String> lista = new ArrayList<String>();
            PDFTextStripper pdfStripper = null;
            PDDocument pdDoc = null;
            COSDocument cosDoc = null;
            File file = new File(ruta);
            PDFParser parser = new PDFParser(new FileInputStream(file));
            parser.parse();
            cosDoc = parser.getDocument();
            pdfStripper = new PDFTextStripper();
            pdDoc = new PDDocument(cosDoc);
            pdfStripper.setStartPage(1);
            String parsedText = pdfStripper.getText(pdDoc);

            Pattern patron = Pattern.compile("[(]DPHU[)].*");
            Matcher m = patron.matcher(parsedText);
            while(m.find() ){
                lista.add(m.group(0));
            }
            return lista;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // Crea una página en blanco dividida en 4 espacios con una cabecera
    public static PDPage creaPagina(PDDocument documento){
        PDPage pagina = new PDPage(PDPage.PAGE_SIZE_A4);
        float altura = pagina.getMediaBox().getHeight();
        float anchura = pagina.getMediaBox().getWidth();
        PDFont font = PDType1Font.HELVETICA_BOLD;
        try {
            PDPageContentStream contentStream = new PDPageContentStream(documento, pagina);
            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.moveTextPositionByAmount(60, altura - 20);
            contentStream.drawString("PROGRAMA DE SOLIDARIDAD");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.moveTextPositionByAmount(anchura / 2.0f + 60, altura - 20);
            contentStream.drawString("PROGRAMA DE SOLIDARIDAD");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.moveTextPositionByAmount(60, altura / 2.0f - 20);
            contentStream.drawString("PROGRAMA DE SOLIDARIDAD");
            contentStream.endText();

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.moveTextPositionByAmount(anchura / 2.0f + 60, altura / 2.0f - 20);
            contentStream.drawString("PROGRAMA DE SOLIDARIDAD");
            contentStream.endText();

            contentStream.drawLine(anchura / 2.0f, 0, anchura / 2.0f, altura);
            contentStream.drawLine(0, altura / 2.0f, anchura, altura / 2.0f);

            contentStream.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return pagina;
    }
}
