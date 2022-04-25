This document outlines the general procedure for deploying a managed Kafka instance (MSK) in AWS. 
The Amazon documentation is pretty bland when it comes to [MSK installation/interaction](https://docs.aws.amazon.com/msk/latest/developerguide/create-cluster.html) so the steps in this document are intended to fill in the gaps left by the AWS documentation.

1. login to the AWS console and navigate to the MSK service page
2. choose create cluster
3. click "custom create" to allow specifying VPC and other settings (otherwise it will deploy in the default VPC for the aws account)
4. enter a name for the cluster
5. select the version of Kafka (2.6.2 is default in MSK but Strimzi deploys 3.0.0)
6. choose broker type (default kafka.m5.large 2cpu/8gb)
7. number of zones == 2 
8. storage set to 100GB (or whatever but decrease it from the 1000GB default)
9. use MSK default configuration (can always change this later)
10. On the next page select the correct VPC and subnets (private subnets for internal access)
11. remove the "default" security group and add the cluster's workers security group to allow internal access (make sure security groups are correct, I wasn't able to change them after creation but is supposed to be possible)
12. on the next page configure the Security settings as intended (Unauthenticated access with plaintext traffic is a good default but can edit/change these after creation as well if trying to setup [SASL/SCRAM](https://docs.aws.amazon.com/msk/latest/developerguide/msk-password.html#msk-password-tutorial))
13. choose which encryption key to use (aws or customer managed)
14. on the next page for Monitoring, all the defaults are fine unless you want to experiment with metrics and logging
15. verify all the settings on the final page before creating (takes ~20 minutes for the MSK kafka cluster to be up)
