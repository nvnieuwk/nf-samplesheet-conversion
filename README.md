# Nextflow Samplesheet Conversion
A script to validate a samplesheet (in CSV, TSV or YAML format) and convert it to a Nextflow channel.

No external dependencies are needed to run this script. All required functionality is already installed along `nextflow`.

To use this script in your pipeline, you simply copy paste the `lib/SamplesheetConversion.groovy` file to the lib folder of your pipeline and then add the `convert` function to your pipeline code with the samplesheet as input:

```nextflow
workflow {
    samplesheet_channel = SamplesheetConversion.convert(
        file("assets/samplesheet.csv", checkIfExists:true),
        file("assets/samplesheet_schema.json", checkIfExists:true)
    )
}
```

`SamplesheetConversion.convert()` requires two inputs: 
1. The samplesheet to parse
2. The schema detailing the validation

```json
{
    "$schema": "http://json-schema.org/draft-07/schema",
    "$id": "https://raw.githubusercontent.com/nvnieuwk/nf-samplesheet-conversion/master/assets/samplesheet_schema.json",
    "title": "Samplesheet validation schema",
    "description": "Schema for the samplesheet used in this pipeline",
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
```

All fields should be present in the `properties` section. These should be in the order you want for the output channel e.g. for this schema the output channel will look like this: `[[id:sample, sample:sample], cram, crai, bed]`.

These are all the parameters you can apply to a field:

| Parameter | Description |
|-----------|-------------|
| pattern | The regex pattern to check on the current field. This will default to `^.*$` when an empty or no pattern is supplied. |
| meta | The current field will be considered a meta value when this parameter is present. This parameter should contain a comma-delimited list of the meta fields to use this value for. |
| unique | Whether or not the field should contain a unique value over the entire samplesheet. The default is `false`. |
| type | The type of the input value - This check isn't currently implemented |
| format | The format of the input value - This check isn't currently implemented |

All names of the required fields should be specified as a list under `required`

To run the [tests](tests/) you need to install [nf-test](https://github.com/askimed/nf-test). And use the command `nf-test test` to run all tests.

Example samplesheets can be found in the [assets](assets/) folder.

DISCLAIMER: This is just a simple script at the moment. All feedback and input are welcome :)
