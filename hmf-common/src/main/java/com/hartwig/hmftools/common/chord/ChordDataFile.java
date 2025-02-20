package com.hartwig.hmftools.common.chord;

import static java.lang.String.format;

import static com.hartwig.hmftools.common.utils.file.FileReaderUtils.createFieldsIndexMap;
import static com.hartwig.hmftools.common.utils.file.FileWriterUtils.checkAddDirSeparator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public final class ChordDataFile
{
    private static final Logger LOGGER = LogManager.getLogger(ChordDataFile.class);

    private static final String DELIM = "\t";

    private static final String FLD_SAMPLE = "sample";
    private static final String FLD_BRCA1 = "p_BRCA1";
    private static final String FLD_BRCA2 = "p_BRCA2";
    private static final String FLD_P_HRD = "p_hrd";
    private static final String FLD_STATUS = "hr_status";
    private static final String FLD_TYPE = "hrd_type";
    private static final String FLD_REMARKS_STATUS = "remarks_hr_status";
    private static final String FLD_REMARKS_TYPE = "remarks_hrd_type";

    private static final String FILE_EXTENSION = ".chord.prediction.tsv";

    public static String generateFilename(final String basePath, final String sample)
    {
        return checkAddDirSeparator(basePath) + sample + FILE_EXTENSION;
    }

    @NotNull
    public static ChordData read(final String filePath) throws IOException
    {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        if(lines.size() != 2)
            throw new IOException(format("invalid chord file(%s), expected 2 lines containing header and data", filePath));

        Map<String,Integer> fieldsIndexMap = createFieldsIndexMap(lines.get(0), DELIM);
        String[] values = lines.get(1).split(DELIM, -1);

        String statusRemarks = values.length >= 8 && fieldsIndexMap.containsKey(FLD_REMARKS_STATUS) ? values[fieldsIndexMap.get(FLD_REMARKS_STATUS)] : "";
        String typeRemarks = values.length >= 8 && fieldsIndexMap.containsKey(FLD_REMARKS_TYPE) ? values[fieldsIndexMap.get(FLD_REMARKS_TYPE)] : "";

        ChordData chordData = ImmutableChordData.builder()
                .BRCA1Value(Double.parseDouble(values[fieldsIndexMap.get(FLD_BRCA1)]))
                .BRCA2Value(Double.parseDouble(values[fieldsIndexMap.get(FLD_BRCA2)]))
                .hrdValue(Double.parseDouble(values[fieldsIndexMap.get(FLD_P_HRD)]))
                .hrStatus(extractHrStatus(values[fieldsIndexMap.get(FLD_STATUS)]))
                .hrdType(values[fieldsIndexMap.get(FLD_TYPE)])
                .remarksHrStatus(statusRemarks)
                .remarksHrdType(typeRemarks)
                .build();

        return chordData;
    }

    static ChordStatus extractHrStatus(@NotNull String hrStatus)
    {
        switch(hrStatus)
        {
            case "cannot_be_determined":
                return ChordStatus.CANNOT_BE_DETERMINED;
            case "HR_proficient":
                return ChordStatus.HR_PROFICIENT;
            case "HR_deficient":
                return ChordStatus.HR_DEFICIENT;
        }
        LOGGER.warn("Unknown CHORD HR status: '{}'", hrStatus);
        return ChordStatus.UNKNOWN;
    }
}
