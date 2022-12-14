import groovyx.gpars.dataflow.DataflowBroadcast
import groovy.json.JsonSlurper
import java.nio.file.Path
import nextflow.Channel


class SamplesheetConversion {
    public static DataflowBroadcast convert(
        Path samplesheet
    ) {

        def schema = new File('assets/samplesheet_schema.json').text
        def Map schemaFields = (Map) new JsonSlurper().parseText(schema).get('items').get('properties')
        def ArrayList allFields = schemaFields.keySet().collect()
        def ArrayList requiredFields = schemaFields.required
        def Integer row_count = 1

        // Header checks
        def ArrayList header
        samplesheet.withReader { header = it.readLine().tokenize(',') }
        def ArrayList differences = allFields.plus(header)
        differences.removeAll(allFields.intersect(header))

        def ArrayList samplesheetDifferences = header.intersect(differences)
        if(samplesheetDifferences.size > 0) {
            throw new Exception("[Samplesheet Error] The samplesheet contains following unwanted field(s): ${samplesheetDifferences}")
        }

        def ArrayList schemaDifferences = allFields.intersect(differences)
        if(schemaDifferences.size > 0) {
            throw new Exception("[Samplesheet Error] The samplesheet must contain '${allFields.join(",")}' as header field(s), but is missing these: ${schemaDifferences}")
        }

        // Field checks + returning the channels
        return Channel.value(samplesheet).splitCsv(header:true, strip:true).map({ row ->

            def ArrayList fieldsToSee = allFields.clone()
            for( field : schemaFields ){
                def String key = field.key
                fieldsToSee.removeAll { it == key }
                println(fieldsToSee)
                println("${field.key}: ${row[field.key]}")
            }

            return output
        })

    }

}