nextflow.enable.dsl = 2

workflow {
    input = SamplesheetConversion.convert(
            file(params.input, checkIfExists:true),
            file(params.schema, checkIfExists:true)
        ).view()

}