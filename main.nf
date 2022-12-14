nextflow.enable.dsl = 2

workflow {
    SamplesheetConversion.convert(file("assets/samplesheet.csv", checkIfExists:true)).view()
}