package aws;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.*;

public class awsTest {

    static AmazonEC2 ec2;

    private static void init() throws Exception {
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                "Cannot load the credentials from the credential profiles file. " +
                "Please make sure that your credentials file is at the correct location (~/.aws/credentials), and is in valid format.",
                e);
        }
        ec2 = AmazonEC2ClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion("ap-southeast-2") // Set the correct AWS region
            .build();
    }

    public static void main(String[] args) throws Exception {
        init();

        Scanner menu = new Scanner(System.in);
        Scanner id_string = new Scanner(System.in);
        int number = 0;

        while (true) {
            System.out.println("\n------------------------------------------------------------");
            System.out.println("           Amazon AWS Control Panel using SDK               ");
            System.out.println("------------------------------------------------------------");
            System.out.println("  1. list instance                2. available zones        ");
            System.out.println("  3. start instance               4. available regions      ");
            System.out.println("  5. stop instance                6. create instance        ");
            System.out.println("  7. reboot instance              8. list images            ");
            System.out.println("  9. execute condor_status        10. list instance types   ");
            System.out.println("  11. get instance details        12. create specific instance ");
            System.out.println("  13. terminate instance          99. quit                  ");
            System.out.println("------------------------------------------------------------");
            System.out.print("Enter an integer: ");

            if (menu.hasNextInt()) {
                number = menu.nextInt();
            } else {
                System.out.println("Please enter a valid integer!");
                break;
            }

            String instance_id = "";

            switch (number) {
                case 1:
                    listInstances();
                    break;
                case 2:
                    availableZones();
                    break;
                case 3:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext())
                        instance_id = id_string.nextLine();
                    if (!instance_id.trim().isEmpty())
                        startInstance(instance_id);
                    break;
                case 4:
                    availableRegions();
                    break;
                case 5:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext())
                        instance_id = id_string.nextLine();
                    if (!instance_id.trim().isEmpty())
                        stopInstance(instance_id);
                    break;
                case 6:
                    System.out.print("Enter ami id: ");
                    String ami_id = "";
                    if (id_string.hasNext())
                        ami_id = id_string.nextLine();
                    if (!ami_id.trim().isEmpty())
                        createInstance(ami_id);
                    break;
                case 7:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext())
                        instance_id = id_string.nextLine();
                    if (!instance_id.trim().isEmpty())
                        rebootInstance(instance_id);
                    break;
                case 8:
                    listImages();
                    break;
                case 9:
                    executeCondorStatus();
                    break;
                case 10:
                    listInstanceTypes();
                    break;
                case 11:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext())
                        instance_id = id_string.nextLine();
                    if (!instance_id.trim().isEmpty())
                        getInstanceDetails(instance_id);
                    break;
                case 12:
                    System.out.print("Enter instance type: ");
                    String instanceType = "";
                    if (id_string.hasNext())
                        instanceType = id_string.nextLine();
                    if (!instanceType.trim().isEmpty())
                        createSpecificInstance(instanceType);
                    break;
                case 13:
                    System.out.print("Enter instance id: ");
                    if (id_string.hasNext())
                        instance_id = id_string.nextLine();
                    if (!instance_id.trim().isEmpty())
                        terminateInstance(instance_id);
                    break;
                case 99:
                    System.out.println("bye!");
                    menu.close();
                    id_string.close();
                    return;
                default:
                    System.out.println("Please select a valid option!");
            }
        }
    }



    public static void createSpecificInstance(String instanceType) {
        System.out.println("Creating an instance of type: " + instanceType);
        try {
            RunInstancesRequest request = new RunInstancesRequest()
                    .withImageId("ami-0a661051b89baba13") // Replace with valid AMI ID
                    .withInstanceType(instanceType)
                    .withMinCount(1)
                    .withMaxCount(1)
                    .withKeyName("cloud-test"); // Replace with your SSH Key Name
            RunInstancesResult response = ec2.runInstances(request);
            System.out.printf("Successfully created instance %s of type %s\n",
                    response.getReservation().getInstances().get(0).getInstanceId(), instanceType);
        } catch (AmazonServiceException ase) {
            System.err.println("Service Exception: " + ase.getMessage());
        }
    }

	public static void listInstances() {
        System.out.println("Listing instances...");
        boolean done = false;
        try {
            DescribeInstancesRequest request = new DescribeInstancesRequest();
            while (!done) {
                DescribeInstancesResult response = ec2.describeInstances(request);
                for (Reservation reservation : response.getReservations()) {
                    for (Instance instance : reservation.getInstances()) {
                        System.out.printf("[id] %s, [AMI] %s, [type] %s, [state] %10s, [monitoring state] %s\n",
                                instance.getInstanceId(), instance.getImageId(), instance.getInstanceType(),
                                instance.getState().getName(), instance.getMonitoring().getState());
                    }
                }
                request.setNextToken(response.getNextToken());
                if (response.getNextToken() == null) {
                    done = true;
                }
            }
        } catch (AmazonServiceException ase) {
            System.err.println("Service Exception: " + ase.getMessage());
        }
    }

    public static void availableZones() {
        System.out.println("Available zones...");
        try {
            DescribeAvailabilityZonesResult availabilityZonesResult = ec2.describeAvailabilityZones();
            for (AvailabilityZone zone : availabilityZonesResult.getAvailabilityZones()) {
                System.out.printf("[id] %s, [region] %s, [zone] %s\n", zone.getZoneId(), zone.getRegionName(),
                        zone.getZoneName());
            }
        } catch (AmazonServiceException ase) {
            System.err.println("Service Exception: " + ase.getMessage());
        }
    }

    public static void availableRegions() {
        System.out.println("Available regions...");
        try {
            DescribeRegionsResult regions_response = ec2.describeRegions();
            for (Region region : regions_response.getRegions()) {
                System.out.printf("[Region] %s, [Endpoint] %s\n", region.getRegionName(), region.getEndpoint());
            }
        } catch (AmazonServiceException ase) {
            System.err.println("Service Exception: " + ase.getMessage());
        }
    }

    public static void startInstance(String instance_id) {
        System.out.printf("Starting instance %s...\n", instance_id);
        try {
            StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instance_id);
            ec2.startInstances(request);
            System.out.printf("Successfully started instance %s\n", instance_id);
        } catch (AmazonServiceException ase) {
            System.err.println("Service Exception: " + ase.getMessage());
        }
    }

    public static void stopInstance(String instance_id) {
        System.out.printf("Stopping instance %s...\n", instance_id);
        try {
            StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instance_id);
            ec2.stopInstances(request);
            System.out.printf("Successfully stopped instance %s\n", instance_id);
        } catch (AmazonServiceException ase) {
            System.err.println("Service Exception: " + ase.getMessage());
        }
    }

    public static void rebootInstance(String instance_id) {
        System.out.printf("Rebooting instance %s...\n", instance_id);
        try {
            RebootInstancesRequest request = new RebootInstancesRequest().withInstanceIds(instance_id);
            ec2.rebootInstances(request);
            System.out.printf("Successfully rebooted instance %s\n", instance_id);
        } catch (AmazonServiceException ase) {
            System.err.println("Service Exception: " + ase.getMessage());
        }
    }
    
    public static void createInstance(String ami_id) {
    System.out.printf("Creating instance with AMI %s...\n", ami_id);
    try {
		RunInstancesRequest run_request = new RunInstancesRequest()
		        .withImageId(ami_id)
		        .withInstanceType(InstanceType.T2Micro) // 기본 인스턴스 유형
		        .withMinCount(1)
		        .withMaxCount(1)
		        .withKeyName("cloud-test"); // SSH 키 이름으로 변경 필요

		RunInstancesResult run_response = ec2.runInstances(run_request);
		String instance_id = run_response.getReservation().getInstances().get(0).getInstanceId();
		System.out.printf("Successfully created instance %s based on AMI %s\n", instance_id, ami_id);
	    } catch (AmazonServiceException ase) {
		System.err.println("Service Exception: " + ase.getMessage());
	    } catch (AmazonClientException ace) {
		System.err.println("Client Exception: " + ace.getMessage());
	    }
   }


    public static void listImages() {
        System.out.println("Listing images...");
        try {
            DescribeImagesRequest request = new DescribeImagesRequest();
            request.getFilters().add(new Filter().withName("name").withValues("aws-htcondor-slave"));
            DescribeImagesResult results = ec2.describeImages(request);
            if (results.getImages().isEmpty()) {
                System.out.println("No images found with the specified filter.");
                return;
            }
            for (Image image : results.getImages()) {
                System.out.printf("[ImageID] %s, [Name] %s, [Owner] %s\n",
                    image.getImageId(), image.getName(), image.getOwnerId());
            }
        } catch (AmazonServiceException ase) {
            System.err.println("Service Exception: " + ase.getMessage());
        }
    }

    public static void executeCondorStatus() {
	    System.out.println("Executing condor_status...");
	    try {
		Process process = Runtime.getRuntime().exec("/usr/bin/condor_status"); // 절대 경로 사용
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
		    System.out.println(line);
		}
		process.waitFor();
		System.out.println("condor_status executed successfully.");
	    } catch (Exception e) {
		System.err.println("Error executing condor_status: " + e.getMessage());
	    }
    }


      public static void listInstanceTypes() {
		System.out.println("Listing instance types...");
		try {
		    // 일부 인스턴스 유형 출력
		    System.out.println("t2.micro, t2.small, m5.large, c5.large, r5.large");
		} catch (Exception e) {
		    System.err.println("Error listing instance types: " + e.getMessage());
		}
      }
      public static void getInstanceDetails(String instance_id) {
		System.out.println("Getting details for instance: " + instance_id);
		try {
		    DescribeInstancesRequest request = new DescribeInstancesRequest().withInstanceIds(instance_id);
		    DescribeInstancesResult response = ec2.describeInstances(request);
		    for (Reservation reservation : response.getReservations()) {
		        for (Instance instance : reservation.getInstances()) {
		            System.out.printf("[Instance ID] %s, [State] %s, [Type] %s, [Launch Time] %s\n",
		                    instance.getInstanceId(), instance.getState().getName(), instance.getInstanceType(),
		                    instance.getLaunchTime());
		        }
		    }
		} catch (AmazonServiceException ase) {
		    System.err.println("Service Exception: " + ase.getMessage());
		}
     }
     
    
    public static void terminateInstance(String instance_id) {
        System.out.println("Terminating instance: " + instance_id);
        try {
            TerminateInstancesRequest request = new TerminateInstancesRequest().withInstanceIds(instance_id);
            ec2.terminateInstances(request);
            System.out.printf("Successfully terminated instance %s\n", instance_id);
        } catch (AmazonServiceException ase) {
            System.err.println("Service Exception: " + ase.getMessage());
        }
    }
   
}

