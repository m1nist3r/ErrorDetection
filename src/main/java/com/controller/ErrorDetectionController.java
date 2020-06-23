package com.controller;

import com.error.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class ErrorDetectionController {
    //region FXML Labels
    @FXML
    private Label methodChoiceLabel;
    @FXML
    private Label inputDataLabel;
    @FXML
    private Label receivedDataLabel;
    @FXML
    private Label correctedDataLabel;
    @FXML
    private Label outputDataLabel;
    @FXML
    private Label outputStringDataLabel;
    @FXML
    private Label summaryLabel;
    @FXML
    private Label dataBitsLabel;
    @FXML
    private Label controlBitsLabel;
    @FXML
    private Label notDetectedErrorsLabel;
    @FXML
    private Label detectedErrorsLabel;
    @FXML
    private Label fixedErrorsLabel;
    @FXML
    private Label transmitterDataLabel;
    //endregion
    //region FXML Controls and Fields
    @FXML
    private Button generateButton;
    @FXML
    private Button encodeButton;
    @FXML
    private Button interfereButton;
    @FXML
    private Button decodeButton;
    @FXML
    private Button clearButton;
    @FXML
    private RadioButton crcRadioButton;
    @FXML
    private RadioButton hammingRadioButton;
    @FXML
    private RadioButton parityRadioButton;
    @FXML
    private ComboBox<String> crcComboBox;
    @FXML
    private Slider generateNumberSlider;
    @FXML
    private Spinner<Integer> interferencesNumberSpinner;
    @FXML
    private TextArea inputData;
    @FXML
    private TextArea transmitterData;
    @FXML
    private TextArea receivedData;
    @FXML
    private TextFlow correctedData;
    @FXML
    private TextArea outputData;
    @FXML
    private TextArea outputString;
    @FXML
    private TextField dataBits;
    @FXML
    private TextField controlBits;
    @FXML
    private TextField detectedErrors;
    @FXML
    private TextField fixedErrors;
    @FXML
    private TextField notDetectedErrors;
    //endregion

    private DataBits inputBits;
    private Parity parityTransmitter;
    private Hamming hammingTransmitter;
    private Crc crcTransmitter;
    private Parity parityReceiver;
    private Hamming hammingReceiver;
    private Crc crcReceiver;
    private CodeBase transmitter;
    private boolean is_String;

    public ErrorDetectionController() {
        Platform.runLater(() -> {
            errorFieldsInitialization();
            settingUp();
        });
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            throw new RuntimeException();
        }
        try {
            new BigDecimal(strNum);
        } catch (NumberFormatException nfe) {
            System.out.println("not number " + nfe.getMessage());
            return false;
        }
        return true;
    }

    private void errorFieldsInitialization() {
        this.inputBits = new DataBits();
        this.parityTransmitter = new Parity();
        this.hammingTransmitter = new Hamming();
        this.crcTransmitter = new Crc();
        this.parityReceiver = new Parity();
        this.hammingReceiver = new Hamming();
        this.crcReceiver = new Crc();
        this.transmitter = parityTransmitter;
        this.is_String = false;
    }

    public String decrypt(String message) {
        //Check to make sure that the message is all 1s and 0s.
        for (int i = 0; i < message.length(); i++) {
            if (message.charAt(i) != '1' && message.charAt(i) != '0') {
                return null;
            }
        }

        //If the message does not have a length that is a multiple of 8, we can't decrypt it
        if (message.length() % 8 != 0) {
            return null;
        }

        //Splits the string into 8 bit segments with spaces in between
        StringBuilder returnString = new StringBuilder();
        StringBuilder decrypt = new StringBuilder();
        for (int i = 0; i < message.length() - 7; i += 8) {
            decrypt.append(message, i, i + 8).append(" ");
        }

        //Creates a string array with bytes that represent each of the characters in the message
        String[] bytes = decrypt.toString().split(" ");
        for (String aByte : bytes) {
            //Decrypts each character and adds it to the string to get the original message
            returnString.append((char) Integer.parseInt(aByte, 2));
        }

        return returnString.toString();
    }

    private void settingUp() {
        transmitterData.setEditable(false);
        receivedData.setEditable(false);
        outputData.setEditable(false);
        dataBits.setEditable(false);
        controlBits.setEditable(false);
        detectedErrors.setEditable(false);
        fixedErrors.setEditable(false);
        notDetectedErrors.setEditable(false);

        encodeButton.setDisable(true);
        interfereButton.setDisable(true);
        decodeButton.setDisable(true);

        parityRadioButton.setSelected(true);
        generateNumberSlider.setMin(8);
        generateNumberSlider.setMax(96);
        generateNumberSlider.setBlockIncrement(8);

        ObservableList<String> options = FXCollections.observableArrayList(
                "ATM", "CRC-12", "CRC-16", "CRC-16 REVERSE", "CRC-32", "SDLC", "SDLC REVERSE"
        );
        crcComboBox.setItems(options);
        crcComboBox.getSelectionModel().select(0);

        ToggleGroup toggleGroup = new ToggleGroup();
        crcRadioButton.setToggleGroup(toggleGroup);
        hammingRadioButton.setToggleGroup(toggleGroup);
        parityRadioButton.setToggleGroup(toggleGroup);
        parityRadioButton.setSelected(true);

        inputData.textProperty().addListener(event -> encodeButton.setDisable(inputData.getText().length() == 0));
        receivedData.textProperty().addListener(event -> {
            if (receivedData.getText().length() == 0) {
                interfereButton.setDisable(true);
                decodeButton.setDisable(true);
            } else {
                interfereButton.setDisable(false);
                decodeButton.setDisable(false);
            }
        });
        interferencesNumberSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));
        interferencesNumberSpinner.getValueFactory().setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer object) {
                return Integer.toString(object);
            }

            @Override
            public Integer fromString(String string) {
                return Integer.parseInt(string);
            }
        });
    }

    public void colorFixedBits(int[] type) {
        StringBuilder sb = new StringBuilder();
        for (Node node : correctedData.getChildren()) {
            if (node instanceof Text) {
                sb.append(((Text) node).getText());
            }
        }
        String str = sb.toString();
        if (str.length() == type.length) {
            ArrayList<Text> style = new ArrayList<>();
            correctedData.getChildren().clear();
            int l = type.length;
            for (int i = 0; i < l; i++) {
                Color colour = switch (type[i]) {
                    case 0 -> Color.GREEN;
                    case 1 -> Color.RED;
                    case 2 -> Color.ORANGE;
                    case 3 -> Color.CYAN;
                    case 4 -> Color.MAGENTA;
                    case 5 -> Color.BLUE;
                    default -> Color.BLACK;
                };
                Text text = new Text();
                text.setText(String.valueOf(str.charAt(i)));
                text.setFill(colour);
                style.add(text);
            }
            correctedData.getChildren().addAll(style);
            Tooltip.install(correctedData, getToolTip(str));
        }
    }

    private int countErrors() {
        String input = transmitterData.getText();
        String output = receivedData.getText();
        if (input.length() != output.length()) {
            return -1;
        } else {
            int errors = 0;
            int l = input.length();
            for (int i = 0; i < l; i++) {
                if (input.charAt(i) != output.charAt(i)) {
                    errors++;
                }
            }
            return errors;
        }
    }

    private void clearCode() {
        transmitterData.setText("");
        receivedData.setText("");
        correctedData.getChildren().clear();
        outputData.setText("");
        dataBits.setText("");
        controlBits.setText("");
        detectedErrors.setText("");
        fixedErrors.setText("");
        notDetectedErrors.setText("");
        outputString.setText("");
        inputData.setText("");
    }

    public void generateButtonActionPerformed() {
        int n = (int) generateNumberSlider.getValue();
        if (n % 8 != 0) {
            n += 8 - n % 8;
            generateNumberSlider.setValue(n);
        }
        inputBits.generate(n);
        inputData.setText(inputBits.toString());
    }

    public void crcRadioButtonActionPerformed() {
        transmitter = crcTransmitter;
        transmitter = crcReceiver;
        this.crcComboBox.setDisable(false);
        clearCode();
    }

    public void hammingRadioButtonActionPerformed() {
        transmitter = hammingTransmitter;
        transmitter = hammingReceiver;
        this.crcComboBox.setDisable(true);
        clearCode();
    }

    public void parityRadioButtonActionPerformed() {
        transmitter = parityTransmitter;
        transmitter = parityReceiver;
        this.crcComboBox.setDisable(true);
        clearCode();
    }

    public void crcComboBoxActionPerformed() {
        if (null != crcComboBox.getItems().get(crcComboBox.getSelectionModel().getSelectedIndex())) {
            switch (crcComboBox.getItems().get(crcComboBox.getSelectionModel().getSelectedIndex())) {
                case "ATM":
                    System.out.println("ATM");
                    crcTransmitter.setKey(Crc.ATM);
                    crcReceiver.setKey(Crc.ATM);
                    break;
                case "CRC-12":
                    System.out.println("CRC-12");
                    crcTransmitter.setKey(Crc.CRC12);
                    crcReceiver.setKey(Crc.CRC12);
                    break;
                case "CRC-16":
                    System.out.println("CRC-16");
                    crcTransmitter.setKey(Crc.CRC16);
                    crcReceiver.setKey(Crc.CRC16);
                    break;
                case "CRC-16 REVERSE":
                    System.out.println("CRC-16 REVERSE");
                    crcTransmitter.setKey(Crc.CRC16_REVERSE);
                    crcReceiver.setKey(Crc.CRC16_REVERSE);
                    break;
                case "CRC-32":
                    System.out.println("CRC-32");
                    crcTransmitter.setKey(Crc.CRC32);
                    crcReceiver.setKey(Crc.CRC32);
                    break;
                case "SDLC":
                    System.out.println("SDLC");
                    crcTransmitter.setKey(Crc.SDLC);
                    crcReceiver.setKey(Crc.SDLC);
                    break;
                case "SDLC REVERSE":
                    System.out.println("SDLC REVERSE");
                    crcTransmitter.setKey(Crc.SDLC_REVERSE);
                    crcReceiver.setKey(Crc.SDLC_REVERSE);
                    break;
                default:
                    break;
            }
        }
    }

    private Tooltip getToolTip(String text) {
        Tooltip tooltip = new Tooltip();
        tooltip.setText(text);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(700);

        return tooltip;
    }

    public void encodeButtonActionPerformed() {
        StringBuilder str = new StringBuilder(inputData.getText());
        inputData.setTooltip(getToolTip(inputData.getText()));
        if (!isNumeric(str.toString())) {
            is_String = true;
            ByteBuffer buffer = StandardCharsets.UTF_8.encode(str.toString());
            byte[] bytes = buffer.array();
            str = new StringBuilder();
            for (byte by : bytes) {
                StringBuilder temp = new StringBuilder(Integer.toBinaryString((by + 256) % 256));
                while (temp.length() < 8) {
                    temp.insert(0, "0");
                }
                str.append(temp);
            }
            inputData.setText(str.toString());
        } else {
            is_String = false;
            if (str.length() == 0) {
                str = new StringBuilder("00000000");
                inputData.setText(str.toString());
            } else if (str.length() % 8 != 0) {
                StringBuilder temp = new StringBuilder();
                int zeros = 8 - str.length() % 8;
                temp.append("0".repeat(zeros));
                str.insert(0, temp.toString());
                inputData.setText(str.toString());
            }
        }
        transmitter.setData(str.toString());
        transmitter.encode();
        transmitterData.setText(transmitter.codeToString());
        transmitterData.setTooltip(getToolTip(transmitter.codeToString()));
        transmitter.setCode(transmitter.codeToString());
        receivedData.setText(transmitter.codeToString());
        receivedData.setTooltip(getToolTip(transmitter.codeToString()));
    }

    public void interfereButtonActionPerformed() {
        transmitter.interfere(Integer.parseInt(interferencesNumberSpinner.getValue().toString()));
        receivedData.setText(transmitter.codeToString());
    }

    public void decodeButtonActionPerformed() {
        transmitter.setCode(receivedData.getText());
        int errors = countErrors();
        transmitter.fix();
        correctedData.getChildren().add(new Text(transmitter.codeToString()));
        colorFixedBits(transmitter.getBitTypes());
        transmitter.decode();
        outputData.setText(transmitter.dataToString());
        if (is_String) {
            outputString.setText(decrypt(transmitter.dataToString()));
            outputString.setTooltip(getToolTip(decrypt(transmitter.dataToString())));
        }
        outputData.setTooltip(getToolTip(transmitter.dataToString()));
        dataBits.setText(Integer.toString(transmitter.getDataBitsNumber()));
        controlBits.setText(Integer.toString(transmitter.getControlBitsNumber()));
        int detected = transmitter.getDetectedErrorsNumber();
        detectedErrors.setText(Integer.toString(detected));
        fixedErrors.setText(Integer.toString(transmitter.getFixedErrorsNumber()));
        notDetectedErrors.setText(Integer.toString(errors - detected));
    }

    public void clearButtonActionPerformed() {
        clearCode();
        fixedErrors.setText("");
        notDetectedErrors.setText("");
        parityTransmitter = new Parity();
        hammingTransmitter = new Hamming();
        crcTransmitter = new Crc();
        parityReceiver = new Parity();
        hammingReceiver = new Hamming();
        crcReceiver = new Crc();
    }
}
