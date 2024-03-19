package it.paolinucs.ramenpersist.service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MasterService {

  @Autowired
  private static EncryptionService encryptionService;

  private static Logger LOG = LoggerFactory.getLogger("MasterPasswordService");

  public void setMasterPassword(String masterPassword) throws IOException {
    FileWriter file = new FileWriter("auth");
    try {
      file.write(encryptionService.encrypt(masterPassword, masterPassword));
      file.close();
    } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
        | BadPaddingException exc) {
      LOG.error("Set master password failed", exc);
    }
  }

  public boolean verifyMasterPassword(String masterPassword)
      throws FileNotFoundException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException,
      NoSuchPaddingException, NoSuchAlgorithmException, IOException {

    FileReader fileReader = new FileReader("auth");
    String fileContent = new String();
    try {
      int i;
      while ((i = fileReader.read()) != -1) {
        fileContent += i;
      }
      LOG.info("Autentication data correctly loaded.");
    } catch (Exception exc) {
      LOG.error("Cannot read authentication data");
      fileReader.close();
      return false;
    }
    fileReader.close();
    return encryptionService.decrypt(fileContent, masterPassword).equals(masterPassword);
  }
}
