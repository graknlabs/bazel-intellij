package com.google.idea.blaze.aspect;

import com.google.common.base.Preconditions;
import com.google.devtools.intellij.aspect.Common;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CargoTomlBuilder {
    final Options options;

    CargoTomlBuilder(Options options) {
        this.options = options;
    }

    static final class Options {
        String name;
        List<Common.ArtifactLocation> sources;
        String binPath;
        String libPath;
        List<String> pathDeps;
        List<String> externalDeps;
        Path outputManifest;
    }

    static Options parseArgs(String[] args) {
        Options options = new Options();
        options.name = OptionParser.parseSingleOption(args, "name", x -> x);
        options.sources =
                OptionParser.parseSingleOption(args, "sources", ArtifactLocationParser::parseList);
        options.binPath = OptionParser.parseSingleOption(args, "bin-path", x -> x);
        options.libPath = OptionParser.parseSingleOption(args, "lib-path", x -> x);
        options.pathDeps = OptionParser.parseSingleOption(args, "path-deps", CargoTomlBuilder::parseStringList);
        options.externalDeps = OptionParser.parseSingleOption(args, "external-deps", CargoTomlBuilder::parseStringList);
        options.outputManifest =
                OptionParser.parseSingleOption(
                        args, "output-manifest", string -> FileSystems.getDefault().getPath(string));
        return options;
    }

    static List<String> parseStringList(String raw) {
        return raw.isEmpty() ? Collections.emptyList() : Arrays.asList(raw.split(":"));
    }

    void writeManifest() throws IOException {
        try (OutputStream out = Files.newOutputStream(options.outputManifest)) {
            out.write(constructManifest().getBytes(StandardCharsets.UTF_8));
        }
    }

    private String constructManifest() {
        return "Hello fellow Rustaceans";
    }

    private static final Logger logger = Logger.getLogger(CargoTomlBuilder.class.getName());

    public static void main(String[] args) throws Exception {
        Options options = parseArgs(args);
        Preconditions.checkNotNull(options.name);
        Preconditions.checkNotNull(options.outputManifest);

        try {
            CargoTomlBuilder cargoTomlBuilder = new CargoTomlBuilder(options);
            cargoTomlBuilder.writeManifest();
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Error writing Cargo manifest", e);
            System.exit(1);
        }
        System.exit(0);
    }
}
