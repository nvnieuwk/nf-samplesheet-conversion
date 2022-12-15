# Nextflow Samplesheet Conversion
A script to validate a samplesheet and convert it to a Nextflow channel.

The script currently checks for a file called `assets/samplesheet_schema.json` which should look something like this:

```json
{
    "$schema": "http://json-schema.org/draft-07/schema",
    "$id": "https://raw.githubusercontent.com/nvnieuwk/nf-samplesheet-conversion/master/assets/samplesheet_schema.json",
    "title": "Samplesheet validation schema",
    "description": "Schema for the samplesheet used in this pipeline",
    "type": "array",
    "items": {
        "type": "object",
        "properties": {
            "sample": {
                "type": "string",
                "meta": "id,sample"
            },
            "cram": {
                "type": "string",
                "pattern": "^\\S+\\.cram$",
                "format": "file-path"
            },
            "crai": {
                "type": "string",
                "pattern": "^\\S+\\.crai$",
                "format": "file-path"
            },
            "bed": {
                "type": "string",
                "pattern": "^\\S+\\.bed$",
                "format": "file-path"
            }
        },
        "required": ["sample", "cram"]
    }
}
```

All fields should be present in the `items.properties` section. These should be in the order you want for the output channel e.g. for this schema the output channel will look like this: `[[id:sample, sample:sample], cram, crai, bed]`.

These are all the parameters you can apply to a field:

| Parameter | Description |
|-----------|-------------|
| pattern | The regex pattern to check on the current field. This will default to `^.*$` when an empty or no pattern is supplied. |
| meta | The current field will be considered a meta value when this parameter is present. This parameter should contain a comma-delimited list of the meta fields to use this value for. |
| type | The type of the input value - This check isn't currently implemented |
| format | The format of the input value - This check isn't currently implemented |

All names of the required fields should be specified as a list under `items.required`

To use this script in your pipeline, you simply copy paste the `lib/SamplesheetConversion.groovy` file to the lib folder of your pipeline and then add the `convert` function to your pipeline code with the samplesheet as input:

```groovy
nextflow.enable.dsl = 2

workflow {
    SamplesheetConversion.convert(file("assets/samplesheet.csv", checkIfExists:true)).view()
}
```

DISCLAIMER: This is just a simple script at the moment. All feedback and input are welcome :)
