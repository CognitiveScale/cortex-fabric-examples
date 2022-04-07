### Data Generator skill 

### Prerequisites
- Docker client
- Cortex client ( installed )

### Sample payload and explanation

```
{
"hadron_kwargs":{
            "HADRON_FEEDBACK_PATH": "s3://project-hadron-cs-repo/13/2022-03-16 20:49:07.602028/members_feedback_v16.parquet",
            "HADRON_MEMBERS_PATH": "s3://project-hadron-cs-repo/13/2022-03-16 20:49:07.602028/members_v14.parquet",
            "HADRON_FLU_RISK_PATH": "s3://project-hadron-cs-repo/13/2022-03-16 20:49:07.602028/members_flu_risk_v14.parquet",
            "mod_tasks": {
                "sor_sim": {
                    "source": 10000000
                }
            }
        },
"domain_contract_repo": "https://raw.githubusercontent.com/project-hadron/hadron-asset-bank/master/contracts/helloworld/members/sor"
}
```

hadron_kwargs : Hadron arguments required for running the domain contract, this usually depends on the domain contract used (in our case the output file path for the data generated and the number of rows )

domain_contract_repo : Path to the Domain contract we want to run

The skill presesnted in this example is a generic skill that uses (hadron)[https://github.com/project-hadron/discovery-transition-ds]to generate data, the idea being that this skill will be reusable for different sets of data generation just by specifying a domain contract, and passing the right `hadron_kwargs`

Domain Contract is a terminology used by hadron users, when hadron is used to write the data generation code (or data preprocessing steps) it captures the operations and stores it in a blob of file known as the domain contract, we can then get rid of the code used to generate the contract and pass it to the controller (hadron controller) for running the sequence of operations stored in the contract.

