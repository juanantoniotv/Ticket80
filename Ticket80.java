/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.reports;

import com.dao.CreditoDAO;
import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.barcode.BarCode;
import com.github.anastaciocintra.escpos.image.Bitonal;
import com.github.anastaciocintra.escpos.image.BitonalThreshold;
import com.github.anastaciocintra.escpos.image.CoffeeImageImpl;
import com.github.anastaciocintra.escpos.image.EscPosImage;
import com.github.anastaciocintra.escpos.image.RasterBitImageWrapper;
import com.github.anastaciocintra.output.PrinterOutputStream;
import com.models.DboCliente;
import com.models.DboNotaVenta;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.print.PrintService;

/**
 *
 * @author antoniodev
 */
public class Ticket80 {
    private Image logobox;
    private int charsize;
    private int sizecol1;
    private int sizecol2;
    private int sizecol3;
    private int sizecol4;
    private int sizecol5;
    private int numticketsize;
    private int id_ticket;
    
    private final DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.JAPAN);
    private final DecimalFormat df = new DecimalFormat("##,###.00", symbols);
    
    private DboNotaVenta dnv;
   
    
    public Ticket80(int num_ticket) {
        charsize = 48;
        sizecol1 = 7;
        sizecol2 = 2;
        sizecol3 = 18;
        sizecol4 = 7;
        sizecol5 = 11;
        numticketsize = 12;
        dnv = new DboNotaVenta();
        id_ticket = num_ticket;
    }
    
    /**
     * Procesa un renglon que contiene los productos según el ancho del ticket
     * y definición de ancho de cada columna.
     * 
     * @param cant
     * @param um
     * @param prod
     * @param precio
     * @param subtotal
     * @return 
     */
    public String productRow(double cant, String um, String prod, double precio, BigDecimal subtotal){
        String fullrow = "";
        fullrow += coladjust(cant, sizecol1, 0);
        fullrow += coladjust(um,sizecol2,1);
        fullrow += coladjust(prod, sizecol3, 1);
        fullrow += coladjust(precio, sizecol4, 1);
        fullrow += coladjust(subtotal, sizecol5, 0);
        return fullrow;
    };
    
    
    /**
     * Ajusta el ancho de cada columna al espacio (size dado).
     * 
     * Método para valores String.
     * 
     * @param cont  String  Contenido de la columna en formato String
     * @param size  int     Ancho en número de caracteres
     * @param space int     Espacios a agregar al final
     * @return 
     */
    public String coladjust(String cont, int size, int space){
        String row = "";
        if (cont.length() <= size){
            int diference = size - cont.length();
            row += cont;
            for(int i=1;i<=diference;i++){
                row += " ";
            }
            for(int i=1;i<=space;i++){
                row += " ";
            }
        }else if (cont.length() > size){
            row = cont.substring(0, Math.min(cont.length(), size - space));
            row += "^";
            for(int i=1;i<=space;i++){
                row += " ";
            }
        }
        return row; 
    }
    
    /**
     * Ajusta el ancho de cada columna al espacio (size dado).
     * 
     * Método para valores dobles.
     * 
     * @param val   double  Valor de la columna
     * @param size  int     Ancho en número de caracteres
     * @param space int     Espacios a agregar al final
     * @return 
     */
    public String coladjust(double val, int size, int space){
        /*
        double amount = 200;
DecimalFormat twoPlaces = new DecimalFormat("0.00");
System.out.println(twoPlaces.format(amount));
        */
        String cont;
        String row = "";
        cont = df.format(val);
        if (cont.length() <= size){
            int diference = size - cont.length();
            for(int i=1;i<=diference;i++){
                row += " ";
            }
            row += cont;
            for(int i=1;i<=space;i++){
                row += " ";
            }
        }
        return row;
    }
    
     /**
     * Ajusta el ancho de cada columna al espacio (size dado).
     * 
     * Método para valores dobles.
     * 
     * @param val   BigDecimal  Valor de la columna
     * @param size  int     Ancho en número de caracteres
     * @param space int     Espacios a agregar al final
     * @return 
     */
    public String coladjust(BigDecimal val, int size, int space){
        String cont;
        String row = "";
        cont = df.format(val);
        if (cont.length() <= size){
            int diference = size - cont.length();
            for(int i=1;i<=diference;i++){
                row += " ";
            }
            row += cont;
            for(int i=1;i<=space;i++){
                row += " ";
            }
        }
        return row;
    }
    
    /**
     * Crea una linea a lo ancho del ticket dado un caracter.
     * 
     * @param letter    String  Carácter de dibujo.
     * @return 
     */
    public String line(String letter){
        String line = "";
        for(int i=1;i<=charsize;i++){
                line += letter;
        }
        return line;
    }
    
    
    /**
     * Rellena un valor entero con n espacios en blanco.
     * 
     * @param id
     * @param spaces
     * @return 
     */
    public String fillValue(int id,int spaces){
        String strId = String.valueOf(id);
        for(int i=strId.length();i<=spaces;i++){
                strId += " ";
        }
        return strId;
    }
    
    /**
     * Imprime el ticket en la impresora especificada.
     * 
     * @param printerName 
     */
    public void print(String printerName){
        
     
        //DecimalFormat twoPlaces = new DecimalFormat("#,###.00");
      
        // get the printer service by name passed on command line...
        //this call is slow, try to use it only once and reuse the PrintService variable.
        PrintService printService = PrinterOutputStream.getPrintServiceByName(printerName);
        EscPos escpos;
        
        dnv.loadNota(id_ticket);
        int idCliente = dnv.getId_cliente();
        DboCliente dbocliente = new DboCliente();
        
        String tmpNumint = dbocliente.getNumint() == null ? "" : dbocliente.getNumint();
        System.out.println(idCliente);
        try {
            escpos = new EscPos(new PrinterOutputStream(printService));
            escpos.setCharacterCodeTable(EscPos.CharacterCodeTable.CP850_Multilingual);
            
            Style title = new Style()
                    .setFontSize(Style.FontSize._1, Style.FontSize._2)
                    .setJustification(EscPosConst.Justification.Center);
            Style centrado = new Style()
                    .setJustification(EscPosConst.Justification.Center);

            Style subtitle = new Style(escpos.getStyle())
                    .setBold(true)
                    .setUnderline(Style.Underline.OneDotThick);
            Style bold = new Style(escpos.getStyle())
                    .setBold(true);
            
            Style chico = new Style()
                    .setFontSize(Style.FontSize._1,Style.FontSize._1)
                    .setFontName(Style.FontName.Font_B)
                    //.setJustification(EscPosConst.Justification.Center)
                    .setLineSpacing(0);
            
            Style producto = new Style()
                .setFontName(Style.FontName.Font_C)
                .setLineSpacing(0);
            
            Style separador = new Style()
                .setLineSpacing(0);
            
            //BARCODE     
            BarCode barcode = new BarCode();
            barcode.setJustification(EscPosConst.Justification.Center);
            barcode.setBarCodeSize(3, 80);
            barcode.setHRIPosition(BarCode.BarCodeHRIPosition.BelowBarCode);
           
            //Para la imagen
            // creating the EscPosImage, need buffered image and algorithm.
            logobox = ImageIO.read(new File("C:\\proyectos\\puntodeventa\\images\\products\\logo300.jpg"));
            BufferedImage  imageBufferedImage = (BufferedImage) logobox;
            
            // this wrapper uses esc/pos sequence: "GS 'v' '0'"
            RasterBitImageWrapper imageWrapper = new RasterBitImageWrapper();
            imageWrapper.setJustification(EscPosConst.Justification.Center);
            
            // using bitonal threshold for dithering with threshold value 60 (clearing)
            Bitonal algorithm = new BitonalThreshold();
            //Bitonal algorithm = new BitonalThreshold(60);
            //Bitonal algorithm = new BitonalThreshold(100);
            EscPosImage escposImage = new EscPosImage(new CoffeeImageImpl(imageBufferedImage), algorithm);
            escpos.write(imageWrapper, escposImage).feed(1);
            escpos.writeLF(title,"NOMBRE NEGOCIO")
                .writeLF(centrado, "Calle")
                .writeLF(centrado,"Ciudad")
                .writeLF(centrado, "Pedidos al Tel")
                .writeLF("________________________________________________")
                .write("Ticket:")
                .write(bold,fillValue(dnv.getId(),12))
                .writeLF("           Fecha: " + dnv.getFecha())
                .writeLF("                 Hora: " + dnv.getHora());
            if (idCliente > 0){
                escpos.write("Cliente: ");
                dbocliente.getCliente(idCliente);
                escpos.writeLF(bold,dbocliente.getNombrecli())
                    .writeLF("Dirección: " + dbocliente.getDireccion() + " " + dbocliente.getNumext() + tmpNumint )
                    .writeLF("           " + dbocliente.getZona() + ", " + dbocliente.getCiudad());
            }else{
                escpos.writeLF("Venta: Público en general.");
            }
            escpos.feed(1)
                .writeLF(separador,"   CANT   PRODUCTO            PRECIO     IMPORTE")
                .writeLF(separador,"------------------------------------------------");
                dnv.getListNVCont().forEach((cont) -> {
                    double total = cont.getCantidad() * cont.getPrecio();
                    try{
                        escpos.writeLF(producto, productRow(cont.getCantidad(), cont.getUmedida(), cont.getDescripcion(), cont.getPrecio(), cont.getTotalneto()) );
                    }catch (IOException ex) {
                        Logger.getLogger(Ticket80.class.getName()).log(Level.SEVERE, null, ex);
                    } 
                });
            escpos.writeLF("------------------------------------------------")
                .writeLF(bold,"                     TOTAL:  $ " + df.format(dnv.getTotalneto()))
                .writeLF(chico,"  Paga: $ " + df.format(dnv.getPaga()))
                .writeLF(chico,"Cambio: $ " + df.format(dnv.getCambio()))
                .feed(1);
            if (idCliente > 0){
                CreditoDAO creditodao = new CreditoDAO();
                BigDecimal saldo = creditodao.getSaldo(idCliente);
                BigDecimal saldoanterior = saldo.subtract(dnv.getTotalneto());
                escpos.writeLF("Saldo anterior: $ " + df.format(saldoanterior))
                   .writeLF("  Saldo actual: $" + df.format(saldo))
                   .feed(1);
            }
            escpos.feed(1)
                .writeLF(centrado,"Gracias por su preferencia")
                .feed(1)
                .write(barcode, "TIC" + dnv.getId())
                .feed(6)
                .cut(EscPos.CutMode.FULL);
            escpos.close();
            
        } catch (IOException ex) {
            Logger.getLogger(Ticket80.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
