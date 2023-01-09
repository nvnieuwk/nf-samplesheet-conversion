import groovyx.gpars.dataflow.DataflowBroadcast
import groovy.json.JsonSlurper
import org.yaml.snakeyaml.Yaml
import java.nio.file.Path
import nextflow.Nextflow
import nextflow.Channel


class SamplesheetConversion {
    public static DataflowBroadcast convert(
        Path samplesheetFile,
        Path schemaFile
    ) {

        def Map schema = (Map) new JsonSlurper().parseText(schemaFile.text)
        def Map schemaFields = schema.get("properties")
        def ArrayList allFields = schemaFields.keySet().collect()
        def ArrayList requiredFields = schema.get("required")
        def Integer rowCount = 1

        def String fileType = getFileType(samplesheetFile)
        def String header
        def DataflowBroadcast samplesheet

        if(fileType == "yaml"){
            samplesheet = Channel.fromList(new Yaml().load((samplesheetFile.text)))
        }
        else {
            header = getHeader(samplesheetFile)
            def String delimiter = fileType == "csv" ? "," : fileType == "tsv" ? "\t" : null

            def ArrayList headerArray = header.tokenize(delimiter)
            def ArrayList differences = allFields.plus(headerArray)
            differences.removeAll(allFields.intersect(headerArray))

            def ArrayList samplesheetDifferences = headerArray.intersect(differences)
            if(samplesheetDifferences.size > 0) {
                throw new Exception("[Samplesheet Error] The samplesheet contains following unwanted field(s): ${samplesheetDifferences}")
            }

            def ArrayList schemaDifferences = allFields.intersect(differences)
            if(schemaDifferences.size > 0) {
                throw new Exception("[Samplesheet Error] The samplesheet must contain '${allFields.join(",")}' as header field(s), but is missing these: ${schemaDifferences}")
            }

            samplesheet = Channel.value(samplesheetFile).splitCsv(header:true, strip:true, sep:delimiter)
        }

        // Field checks + returning the channels
        def Map uniques = [:]

        return samplesheet.map({ row ->

            rowCount++
            def Map meta = [:]
            def ArrayList output = []

            for( field : schemaFields ){
                def String key = field.key
                def String regexPattern = field.value.pattern && field.value.pattern != '' ? field.value.pattern : '^.*$'
                def String metaNames = field.value.meta
                
                def String input = row[key]

                if(input == null && fileType == "yaml"){
                    input = ""
                }

                if(input == null){
                    throw new Exception("[Samplesheet Error] Line ${rowCount} does not contain an input for field '${key}'.")
                }
                else if(input == "" && key in requiredFields){
                    throw new Exception("[Samplesheet Error] Line ${rowCount} contains an empty input for required field '${key}'.")
                }
                else if(field.value.unique){
                    if(!(key in uniques)){uniques[key] = []}
                    if(input in uniques[key]){
                        throw new Exception("[Samplesheet Error] The '${key}' value needs to be unique. '${input}' was found twice in the samplesheet.")
                    }
                    uniques[key].add(input)
                }
                else if(!(input ==~ regexPattern) && input != '') {
                    throw new Exception("[Samplesheet Error] The '${key}' value on line ${rowCount} does not match the pattern '${regexPattern}'.")
                }
                
                if(metaNames) {
                    for(name : metaNames.tokenize(',')) {
                        meta[name] = input != '' ? input.replace(' ', '_') : field.value.default ?: null
                    }
                }
                else {
                    def inputFile = input != '' ? Nextflow.file(input) : field.value.default ? Nextflow.file(field.value.default) : []
                    if( inputFile != [] && !inputFile.exists() ){
                        throw new Exception("[Samplesheet Error] The '${key}' file (${input}) on line ${rowCount} does not exist.")
                    }
                    output.add(inputFile)
                    
                }
            }
            output.add(0, meta)
            return output
        })

    }

    private static String getFileType(
        Path samplesheetFile
    ) {
        def String extension = samplesheetFile.getExtension()
        if (extension in ["csv", "tsv", "yml", "yaml"]) {
            return extension == "yml" ? "yaml" : extension
        }

        def String header = getHeader(samplesheetFile)

        def Integer commaCount = header.count(",")
        def Integer tabCount = header.count("\t")

        if ( commaCount == tabCount ){
            throw new Exception("[Samplesheet Error] Could not derive file type from ${samplesheetFile}. Please specify the file extension (CSV, TSV, YML and YAML are supported).")
        }
        if ( commaCount > tabCount ){
            return "csv"
        }
        else {
            return "tsv"
        }
    }

    private static String getHeader(
        Path samplesheetFile
    ) {
        def String header
        samplesheetFile.withReader { header = it.readLine() }
        return header
    }

}