package com.hartwig.hmftools.esvee.assembly.alignment;

import java.util.List;

import org.broadinstitute.hellbender.utils.bwa.BwaMemAlignment;

public interface Aligner
{
    List<BwaMemAlignment> alignSequence(final byte[] bases);
}