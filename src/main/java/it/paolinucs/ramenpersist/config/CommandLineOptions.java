package it.paolinucs.ramenpersist.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.paolinucs.ramenpersist.service.PersistenceService;
import it.paolinucs.ramenpersist.service.MasterService;

@Component
public class CommandLineOptions {

    private Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private PersistenceService jsonService;

    @Autowired
    private MasterService masterService;

    private final Map<String, Consumer<CommandLine>> optionActions = new HashMap<>();


    //TODO: fix multiple arguments command.

    public CommandLineOptions() {
        optionActions.put("h", this::printHelp);
        optionActions.put("u", this::uploadData);
        optionActions.put("m", this::setMasterPassword);
        optionActions.put("d", this::retrieveAllPayloads);
    }

    public void handleOptions(String[] args) throws ParseException {
        Options options = createOptions();
        CommandLineParser parser = new DefaultParser();

        try {
            CommandLine cmd = parser.parse(options, args);
            Option[] cmdOptions = cmd.getOptions();

            if (cmdOptions.length > 0) {
                Option option = cmdOptions[0]; // Prendi la prima opzione
                optionActions.getOrDefault(option.getOpt(), this::handleUnknownOption).accept(cmd);
            } else {
                LOG.warn("No option provided");
                System.exit(1);
            }
        } catch (ParseException exc) {
            LOG.error("Error parsing command line options!");
        }
    }

    private Options createOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "Print help message");
        options.addOption("u", "up", true,
                "Upload payload's data (JSON)\nUsage: <.jar-directory> -u '{\"your\":\"json\"}'");
        options.addOption("d", "down", false, "Retrieve payloads' data");
        options.addOption("m", "master", false, "Set a new master password");
        options.addOption("r", "remove", true,
                "Delete one request data (or all)\nUsage <.jar-directory> -r all | <.jar-directory> -r <uuid>");
        return options;
    }

    private void printHelp(CommandLine cmd) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ramen-persist", createOptions());
        System.exit(0);
    }

    private void uploadData(CommandLine cmd) {
        LOG.info("Uploading new data");
        String json = cmd.getOptionValue("u", "{}");
        String masterPassword = ""; // <== qui la logica per ottenere la password
        jsonService.save(json, masterPassword);
        System.exit(0);
    }

    private void setMasterPassword(CommandLine cmd) {
        LOG.info("Set new master-password");
        String newPassword = cmd.getOptionValue("m");
        LOG.info("New Password: {}", newPassword);
        try {
            masterService.setMasterPassword(newPassword);
            masterService.resetData();
        } catch (IOException exc) {
            LOG.error("Cannot set new master password!");
            System.exit(1);
        }
        System.exit(0);
    }

    private void handleUnknownOption(CommandLine cmd) {
        System.out.println("Unknown option.");
        System.exit(1);
    }

    public void retrieveAllPayloads(CommandLine cmd) {
        try {
            System.out.println(jsonService.importAll());
            System.exit(0);
        } catch (IOException exc) {
            System.exit(1);
        }
    }

}
