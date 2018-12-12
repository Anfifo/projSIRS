package filesystem;

import filesystem.data.EncryptedFileWrapper;
import filesystem.data.FileSystemMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.Map;

@RestController
@RequestMapping("/operations")
public class FileSystemController {

    @RequestMapping(value = "/download")
    public ResponseEntity<FileSystemMessage> download(@Valid @RequestBody FileSystemMessage fMsg) throws IOException, ClassNotFoundException {

        if(!checkInput(fMsg))
            throw new IOException();

        FileSystemMessage message = new FileSystemMessage();

        //Check if the token is valid

        if(!FileSystemInterface.validateToken(fMsg.getUserName(), fMsg.getToken()))
            return new ResponseEntity<FileSystemMessage>(HttpStatus.PRECONDITION_FAILED);

        message.setFiles(FileSystemInterface.download(fMsg.getUserName()));
        return new ResponseEntity<FileSystemMessage>(message, HttpStatus.OK);
    }

    @PostMapping(value = "/upload")
    public ResponseEntity upload(@Valid @RequestBody FileSystemMessage fMsg) throws IOException {
        System.out.println("RECEIVED");
        if(!checkInput(fMsg))
            throw new IOException();
//        System.out.println(file.get(0).getFileName());
//        System.out.println(Arrays.toString(file.get(0).getFile()));
        //Check if the token is valid

        System.out.println(fMsg.getToken());
        System.out.println(fMsg.getUserName());
        if(!FileSystemInterface.validateToken(fMsg.getUserName(), fMsg.getToken()))
            return new ResponseEntity(HttpStatus.PRECONDITION_FAILED);

        System.out.println("Validated token");
        FileSystemInterface.upload(fMsg.getFiles());
        System.out.println("DONE");
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/share")
    public ResponseEntity share(@Valid@RequestBody FileSystemMessage fMsg) throws IOException, ClassNotFoundException {
        FileSystemMessage message = new FileSystemMessage();

        checkInput(fMsg);
        if(!FileSystemInterface.validateToken(fMsg.getUserName(), fMsg.getToken()))
            return new ResponseEntity(HttpStatus.PRECONDITION_FAILED);

        FileSystemInterface.share(fMsg.getUserName(),fMsg.getUserToShareWith(),fMsg.getFiles()[0]);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/getoldversion")
    public ResponseEntity getoldversion(@Valid@RequestBody FileSystemMessage fMsg) throws IOException, ClassNotFoundException {

        checkInput(fMsg);
        FileSystemMessage message = new FileSystemMessage();
        System.out.println(fMsg.getUserName());
        System.out.println(fMsg.getBackUpFileName());

        if(!FileSystemInterface.validateToken(fMsg.getUserName(), fMsg.getToken()))
            return new ResponseEntity(HttpStatus.PRECONDITION_FAILED);

        message.setFiles(new EncryptedFileWrapper[]{ FileSystemInterface.getOldVersion(fMsg.getUserName(),fMsg.getBackUpFileName(),fMsg.getCorrupted())});
        message.setUserName(fMsg.getUserName());
        return new ResponseEntity<FileSystemMessage>(message , HttpStatus.OK);
    }

    @RequestMapping(value = "/test")
    public String test(){
        return "works";
    }

    public Boolean checkInput(FileSystemMessage fMsg){
        if (fMsg.getUserName()!= null ) {
            if(!fMsg.getUserName().matches("[a-zA-Z0-9]*")) {
                System.out.println("Bad username");
                return false;
            }
        }
        if (fMsg.getUserToShareWith()!= null){
            if(!fMsg.getUserToShareWith().matches("[a-zA-Z0-9]*")) {
                System.out.println("Bad username to share with");
                return false;
            }
        }
        if(fMsg.getBackUpFileName()!=null){
            if(!fMsg.getUserName().matches("[a-zA-Z0-9]*"))
                System.out.println("Bad filename for backup");
                return false;
        }
        EncryptedFileWrapper[] files = fMsg.getFiles();

        if(files != null) {
            for (int i = 0; i < files.length; i++) {
                if (!files[i].getFileCreator().matches("[a-zA-Z0-9]*")) {
                    System.out.println("Bad FileCreatorName");
                    return false;
                }
                if (!files[i].getFileName().matches("[a-zA-Z0-9._-]*")) {
                    System.out.println("Bad FileName");
                    return false;
                }
            }
        }
        System.out.println("Passed validation");
        return true;
    }
}

