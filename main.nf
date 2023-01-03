nextflow.enable.dsl = 2

process MODULE {
    input:
    tuple val(meta), path(cram), path(crai), path(bed)

    output:
    tuple val(meta), path("*.txt"), emit: txt

    script:
    """
    ls * > out.txt
    """
}

workflow {
    input = SamplesheetConversion.convert(
            file(params.input, checkIfExists:true),
            file(params.schema, checkIfExists:true)
        ).view()
    MODULE(
        input
    )

}