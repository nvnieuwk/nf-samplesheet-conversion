# Nextflow Samplesheet Conversion
Progress of this repository will be added to the `nf-validation` plugin for Nextflow!

A script to validate a samplesheet (in CSV, TSV or YAML format) and convert it to a Nextflow channel.

No external dependencies are needed to run this script. All required functionality is already installed along `nextflow`.

To use this script in your pipeline, you simply copy paste the `lib/SamplesheetConversion.groovy` file to the lib folder of your pipeline and then add the `convert` function to your pipeline code with the samplesheet as input:


```nextflow
workflow {
    samplesheet_channel = SamplesheetConversion.convert(
        file("${projectDir}/assets/samplesheet.csv", checkIfExists:true),
        file("${projectDir}/assets/samplesheet_schema.json", checkIfExists:true)
    )
}
```

`SamplesheetConversion.convert()` requires two inputs: 
1. The samplesheet to parse
2. The schema detailing the validation

## The schema
This is an example schema containing all features. More info can be found under the example.

Example samplesheets can be found in the [assets](assets/) folder.

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
    "required": ["sample", "cram"],
    "dependentRequired": {
        "bed": ["crai"]
    }
}
```
### `properties`
All fields should be present in the `properties` section. These should be in the order you want for the output channel e.g. for this schema the output channel will look like this: `[[id:sample, sample:sample], cram, crai, bed]`.

These are all the parameters you can apply to a field. All fields should be specified under `properties` in the schema.:

| Parameter | Description |
|-----------|-------------|
| pattern | The regex pattern to check on the current field. This will default to `^.*$` when an empty or no pattern is supplied. |
| meta | The current field will be considered a meta value when this parameter is present. This parameter should contain a comma-delimited list of the meta fields to use this value for. |
| unique | Whether or not the field should contain a unique value over the entire samplesheet. The default is `false`. |
| type | The type of the input value. The input will be automatically converted to this type. To see all possibilities go to [Types](#types) |
| format | The format of the input value, only useable if the type is `string`. To see all possibilities go to [Formats](#formats) |


#### Types

Following table shows all currently implemented types with a description.

| Type | Description |
|-----------|-------------|
| string | A string field can consist of all possible characters except for the delimiters when using CSV or TSV files. |
| integer | An integer field can only contain numeral characters and decimal points. |
| boolean | A boolean field can only contain `true` or `false`. This is case insensitive, but will transform this value to a Groovy Boolean type (`true` or `false`). |

#### Formats

Formats can be used to check string values for certain properties. The input will also be transformed to the correct type.

Following table shows all currently implemented formats with a description.

| Type | Description |
|-----------|-------------|
| file-path | Automatically checks if the file exists and transforms the `String` type to a `Nextflow.File` type, which is usable in Nextflow processes as a `path` input |
| directory-path | Automatically checks if the directory exists and transforms the `String` type to a `Nextflow.File` type, which is usable in Nextflow processes as a `path` input. This is currently synonymous for `file-path`. |

### `required`
All names of the required fields should be specified as an array under `required`.


### `dependentRequired`
All dependencies should be defined under `dependentRequired` as a map with key:value pairs, where the key is the row to check the dependencies for if a value has been given and value is a list of rows to check as a dependency for the key. e.g.:
```json
"dependentRequired": {
    "bed": ["crai"]
}
```
This code will check if the `bed` field has been filled in and if so, it will check for the dependency `crai` and give an error if the dependency is empty or not supplied.

## Tests
To run the [tests](tests/) you need to install [nf-test](https://github.com/askimed/nf-test). And use the command `nf-test test` to run all tests.


DISCLAIMER: This is just a simple script at the moment. All feedback and input are welcome :)
