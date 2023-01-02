nextflow.enable.dsl = 2

workflow {
    SamplesheetConversion.convert(file(params.input, checkIfExists:true)).view()
}