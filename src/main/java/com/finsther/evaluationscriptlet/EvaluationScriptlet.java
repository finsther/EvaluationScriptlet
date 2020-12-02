package com.finsther.evaluationscriptlet;

import java.util.regex.Pattern;
import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import net.sf.jasperreports.engine.fill.JRFillField;

/** @author Cesar Salas */
public class EvaluationScriptlet extends JRDefaultScriptlet {

  public Object evaluateField(String fieldName) throws JRScriptletException {
    JRFillField field = (JRFillField) this.fieldsMap.get(fieldName);

    if (field == null) {
      throw new JRScriptletException("Field not found : " + fieldName);
    }

    StackTraceElement[] stack = new Exception().getStackTrace();

    if (stack.length < 2) {
      throw new JRScriptletException("Unable to obtain current call stack");
    }

    String evaluateMethod = stack[1].getMethodName();
    Object value;

    if (evaluateMethod.startsWith("evaluateOld")) {
      value = field.getOldValue();
    } else if (evaluateMethod.startsWith("evaluate")) {
      value = field.getValue();
    } else {
      throw new JRScriptletException(
          "Unable to determine evaluation type from method name " + evaluateMethod);
    }
    return value;
  }

  public String priorityDescription() throws JRScriptletException {
    String id = (String) this.getFieldValue("id");
    return "Does it work " + id;
  }

  public String convertNumberToWords(String number, boolean toUpperCase) {
    NumberToWords ntw = new NumberToWords();

    return ntw.convertir(number, toUpperCase);
  }
}

class NumberToWords {
  private static final String[] UNIDADES = {
    "", "un ", "dos ", "tres ", "cuatro ", "cinco ", "seis ", "siete ", "ocho ", "nueve "
  };
  private static final String[] DECENAS = {
    "diez ",
    "once ",
    "doce ",
    "trece ",
    "catorce ",
    "quince ",
    "dieciseis ",
    "diecisiete ",
    "dieciocho ",
    "diecinueve",
    "veinte ",
    "treinta ",
    "cuarenta ",
    "cincuenta ",
    "sesenta ",
    "setenta ",
    "ochenta ",
    "noventa "
  };
  private static final String[] CENTENAS = {
    "",
    "ciento ",
    "doscientos ",
    "trecientos ",
    "cuatrocientos ",
    "quinientos ",
    "seiscientos ",
    "setecientos ",
    "ochocientos ",
    "novecientos "
  };

  public String convertir(String numero, boolean mayusculas) {
    String literal = "";
    String decimales;
    /* si el numero utiliza (.) en lugar de (,) -> se reemplaza */
    numero = numero.replace(".", ",");
    /* si el numero no tiene parte decimal, se le agrega ,00 */
    if (numero.indexOf(",") == -1) {
      numero = numero + ",00";
    }
    /* validar formato de entrada -> 0,00 y 999 999 999 999,00 */
    if (Pattern.matches("\\d{1,12},\\d{1,2}", numero)) {
      /* dividir el numero 000000000000,00 -> entero y decimal */
      String[] num = numero.split(",");
      /* formato de numero decimal */
      decimales = String.format("Pesos %s/100 M.N.", num[1]);
      /* se convierte el numero a literal */
      if (Integer.parseInt(num[0]) == 0) {
        literal = "cero ";
      } else if (Integer.parseInt(num[0]) > 999999999) {
        literal = getBillones(num[0]);
      } else if (Integer.parseInt(num[0]) > 999999) {
        literal = getMillones(num[0]);
      } else if (Integer.parseInt(num[0]) > 999) {
        literal = getMiles(num[0]);
      } else if (Integer.parseInt(num[0]) > 99) {
        literal = getCentenas(num[0]);
      } else if (Integer.parseInt(num[0]) > 9) {
        literal = getDecenas(num[0]);
      } else { // sino unidades -> 9
        literal = getUnidades(num[0]);
      }
      /* retornar el resultado en mayusculas o minusculas */
      if (mayusculas) {
        return (literal + decimales).toUpperCase();
      } else {
        return (literal + decimales);
      }
    } else {
      return "error en el formato numerico";
    }
  }

  private String getUnidades(String numero) {
    /* remover 0 a la izquierda -> 09 = 9 o 009=9 */
    String num = numero.substring(numero.length() - 1);
    return UNIDADES[Integer.parseInt(num)];
  }

  private String getDecenas(String num) {
    int n = Integer.parseInt(num);

    if (n < 10) {
      /* casos como -> 01 - 09 */
      return getUnidades(num);
    } else if (n > 19) {
      /* para 20...99 */
      String unidades = getUnidades(num);

      if (unidades.equals("")) {
        return DECENAS[Integer.parseInt(num.substring(0, 1)) + 8];
      } else {
        return DECENAS[Integer.parseInt(num.substring(0, 1)) + 8] + "y " + unidades;
      }
    } else {
      /* numeros entre 11 y 19 */
      return DECENAS[n - 10];
    }
  }

  private String getCentenas(String num) {
    if (Integer.parseInt(num) > 99) {
      if (Integer.parseInt(num) == 100) {
        /* caso especial */
        return " cien ";
      } else {
        return CENTENAS[Integer.parseInt(num.substring(0, 1))] + getDecenas(num.substring(1));
      }
    } else {
      /* remover 0's antes de convertir a decenas */
      return getDecenas(Integer.parseInt(num) + "");
    }
  }

  private String getMiles(String numero) {
    /* obtener centenas */
    String centenas = numero.substring(numero.length() - 3);
    /* obtener miles */
    String miles = numero.substring(0, numero.length() - 3);
    String n = "";
    /* comprobar valor entero de miles */
    if (Integer.parseInt(miles) > 0) {
      n = getCentenas(miles);
      return n + "mil " + getCentenas(centenas);
    } else {
      return "" + getCentenas(centenas);
    }
  }

  private String getMillones(String numero) {
    /* obtener miles */
    String miles = numero.substring(numero.length() - 6);
    /* obtener millones */
    String millon = numero.substring(0, numero.length() - 6);
    String n = "";

    if (millon.length() > 1) {
      n = getCentenas(millon) + "millones ";
    } else {
      n = getUnidades(millon) + "millón ";
    }
    /* return string */
    return n + getMiles(miles);
  }

  private String getBillones(String numero) {
    /* obtener millones */
    String millones = numero.substring(numero.length() - 9);
    /* obtener billones */
    String billon = numero.substring(0, numero.length() - 9);
    String n = "";

    if (billon.length() > 1) {
      n = getCentenas(billon) + "billones ";
    } else {
      n = getUnidades(billon) + "billón ";
    }
    /* return string */
    return n + getMillones(millones);
  }
}
