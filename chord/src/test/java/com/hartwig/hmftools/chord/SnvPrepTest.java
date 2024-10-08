package com.hartwig.hmftools.chord;

import static com.hartwig.hmftools.chord.ChordTestUtils.EMPTY_SAMPLE;
import static com.hartwig.hmftools.chord.ChordTestUtils.INPUT_VCF_DIR;
import static com.hartwig.hmftools.chord.ChordTestUtils.MINIMAL_SAMPLE;
import static com.hartwig.hmftools.chord.ChordTestUtils.TMP_OUTPUT_DIR;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hartwig.hmftools.chord.common.MutTypeCount;
import com.hartwig.hmftools.chord.snv.SnvPrep;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SnvPrepTest
{
    @Before
    public void setup()
    {
        new File(TMP_OUTPUT_DIR).mkdir();
    }

    @After
    public void teardown() throws IOException
    {
        FileUtils.deleteDirectory(new File(TMP_OUTPUT_DIR));
    }

    @Test
    public void canPrepSnvs()
    {
        ChordConfig config = new ChordConfig.Builder()
                .sampleIds(List.of(EMPTY_SAMPLE))
                .purpleDir(INPUT_VCF_DIR)
                .outputDir(TMP_OUTPUT_DIR)
                .build();

        SnvPrep prep = new SnvPrep(config);

        List<MutTypeCount> actualContextCounts = prep.countMutationContexts(MINIMAL_SAMPLE);
        //actualContextCounts.forEach(System.out::println);

        // There are 96 contexts in total, but only test the first few
        List<MutTypeCount> firstExpectedContextCounts = List.of(
                new MutTypeCount("A[C>A]A", 8),
                new MutTypeCount("A[C>A]C", 1),
                new MutTypeCount("A[C>A]G", 3),
                new MutTypeCount("A[C>A]T", 2),
                new MutTypeCount("C[C>A]A", 87),
                new MutTypeCount("C[C>A]C", 10),
                new MutTypeCount("C[C>A]G", 21),
                new MutTypeCount("C[C>A]T", 48),
                new MutTypeCount("G[C>A]A", 3),
                new MutTypeCount("G[C>A]C", 1),
                new MutTypeCount("G[C>A]G", 0),
                new MutTypeCount("G[C>A]T", 1),
                new MutTypeCount("T[C>A]A", 15),
                new MutTypeCount("T[C>A]C", 7),
                new MutTypeCount("T[C>A]G", 1),
                new MutTypeCount("T[C>A]T", 12),
                new MutTypeCount("A[C>G]A", 2),
                new MutTypeCount("A[C>G]C", 4),
                new MutTypeCount("A[C>G]G", 0),
                new MutTypeCount("A[C>G]T", 3)
        );

        List<MutTypeCount> firstActualContextCounts = new ArrayList<>();
        for(int i = 0; i < firstExpectedContextCounts.size(); i++)
        {
            firstActualContextCounts.add(actualContextCounts.get(i));
        }

        assertEquals(firstExpectedContextCounts, firstActualContextCounts);
    }
}
