package fixeh.cli;

import com.beust.jcommander.converters.IParameterSplitter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SemiColonParameterSplitterWithTrimming implements IParameterSplitter {
    @Override
    public List<String> split(String value) {
        return Arrays.stream(value.replaceAll("\n", "").split(";"))
            .map(String::trim)
            .collect(Collectors.toList());
    }
}
