package com.hartwig.hmftools.sage.common;

import static java.lang.Math.min;

import static com.hartwig.hmftools.common.test.GeneTestUtils.CHR_1;
import static com.hartwig.hmftools.sage.common.TestUtils.QUALITY_CALCULATOR;
import static com.hartwig.hmftools.sage.common.TestUtils.TEST_CONFIG;
import static com.hartwig.hmftools.sage.common.TestUtils.TEST_SAMPLE;
import static com.hartwig.hmftools.sage.common.VariantReadContextBuilder.determineAltIndexUpper;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.hmftools.sage.candidate.Candidate;
import com.hartwig.hmftools.sage.evidence.ReadContextCounter;

import htsjdk.samtools.CigarElement;
import htsjdk.samtools.CigarOperator;

public final class VariantUtils
{
    // Simple variant creation
    public static SimpleVariant createSimpleVariant(int position)
    {
        return new SimpleVariant(CHR_1, position, "A", "C");
    }

    public static SimpleVariant createSimpleVariant(int position, final String ref, final String alt)
    {
        return new SimpleVariant(CHR_1, position, ref, alt);
    }

    // Variant read context creation               0123456789
    public static final String TEST_LEFT_FLANK  = "ACCGCTGACT"; // DEFAULT_READ_CONTEXT_FLANK_SIZE
    public static final String TEST_RIGHT_FLANK = "CTGAGACTCA";
    public static final String TEST_LEFT_CORE = "AC"; // based on MIN_CORE_DISTANCE
    public static final String TEST_RIGHT_CORE = "GT";

    // Read context counter creation

    public static VariantReadContext createReadContext(int position, final String ref, final String alt)
    {
        return createReadContext(createSimpleVariant(position, ref, alt));
    }

    public static VariantReadContext createReadContext(final SimpleVariant variant)
    {
        return createReadContext(variant, TEST_LEFT_CORE, TEST_RIGHT_CORE);
    }

    public static VariantReadContext createReadContext(
            final SimpleVariant variant, final String readBases, int leftCoreIndex, int varIndex, int rightCoreIndex)
    {
        String leftFlank = readBases.substring(0, leftCoreIndex);
        String leftCore = readBases.substring(leftCoreIndex, varIndex);
        String rightCore = readBases.substring(varIndex + 1, rightCoreIndex + 1);
        String rightFlank = readBases.substring(rightCoreIndex + 1);
        return createReadContext(variant, leftCore, rightCore, leftFlank, rightFlank);
    }

    public static VariantReadContext createReadContext(
            final SimpleVariant variant, final String leftCore, final String rightCore)
    {
        return createReadContext(variant, leftCore, rightCore, TEST_LEFT_FLANK, TEST_RIGHT_FLANK);
    }

    public static VariantReadContext createReadContext(
            final SimpleVariant variant, final String leftCore, final String rightCore, final String leftFlank, final String rightFlank)
    {
        // create a context with no repeat or homology
        int coreIndexStart = leftFlank.length();
        int varReadIndex = coreIndexStart + leftCore.length();
        int coreIndexEnd = varReadIndex + variant.alt().length() - 1 + rightCore.length();
        String refBases = leftCore + variant.ref() + rightCore;
        String readBases = leftFlank + leftCore + variant.alt() + rightCore + rightFlank;

        int alignmentStart = variant.Position - varReadIndex;

        int coreRightPosStart = variant.Position + min(variant.ref().length(), variant.alt().length());
        int alignmentEnd = coreRightPosStart + rightCore.length() + rightFlank.length() - 1;

        List<CigarElement> readCigar = List.of(new CigarElement(readBases.length(), CigarOperator.M));

        int corePositionStart = variant.Position - leftCore.length();
        int corePositionEnd = variant.Position + rightCore.length();

        int altIndexLower = varReadIndex;
        int altIndexUpper = determineAltIndexUpper(variant, varReadIndex, null);

        return new VariantReadContext(
                variant, alignmentStart, alignmentEnd, refBases.getBytes(), readBases.getBytes(), readCigar, coreIndexStart, varReadIndex,
                coreIndexEnd, null, null, Collections.emptyList(), altIndexLower, altIndexUpper, corePositionStart, corePositionEnd);
    }

    // Read context counter
    public static ReadContextCounter createReadCounter(final int id, final VariantReadContext readContext)
    {
        return new ReadContextCounter(
                id, readContext, VariantTier.LOW_CONFIDENCE,
                100, 1, TEST_CONFIG, QUALITY_CALCULATOR, TEST_SAMPLE);
    }

    // Sage variant creation
    public static SageVariant createSageVariant(int position, final String ref, final String alt)
    {
        SimpleVariant variant = createSimpleVariant(position, ref, alt);

        VariantReadContext readContext = createReadContext(variant);
        return createSageVariant(readContext);
    }

    public static SageVariant createSageVariant(final VariantReadContext readContext)
    {
        ReadContextCounter readCounter = new ReadContextCounter(
                0, readContext, VariantTier.LOW_CONFIDENCE, 100, 1,
                TestUtils.TEST_CONFIG, QUALITY_CALCULATOR, null);

        List<ReadContextCounter> tumorCounters = Lists.newArrayList(readCounter);

        Candidate candidate = new Candidate(
                VariantTier.HIGH_CONFIDENCE, tumorCounters.get(0).readContext(), 1, 1);

        List<ReadContextCounter> normalCounters = Lists.newArrayList();

        return new SageVariant(candidate, normalCounters, tumorCounters);
    }

    public static SageVariant sageVariantFromReadContextCounter(final ReadContextCounter readContextCounter)
    {
        Candidate candidate = new Candidate(
                VariantTier.HIGH_CONFIDENCE, readContextCounter.readContext(), 1, 1);

        return new SageVariant(candidate, Collections.emptyList(), Lists.newArrayList(readContextCounter));
    }
}
