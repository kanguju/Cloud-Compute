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
	 // AWS 자격 증명을 로드하기 위해 ProfileCredentialsProvider를 생성합니다.
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
	    // 자격 증명을 가져옵니다.
            credentialsProvider.getCredentials();
        } catch (Exception e) {
		// 자격 증명을 로드할 수 없을 때 예외를 발생시킵니다.
            throw new AmazonClientException(
                "Cannot load the credentials from the credential profiles file. " +
                "Please make sure that your credentials file is at the correct location (~/.aws/credentials), and is in valid format.",
                e);
        }

	 // Amazon EC2 클라이언트를 초기화합니다.
   	 // 올바른 AWS 자격 증명과 리전 설정이 필요합니다.
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
            System.out.println("  13. terminate instance          		                 ");
            System.out.println("  14. listSecurityGroups          99. quit                  ");
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
		    // 사용자가 입력한 값이 있는지 확인
                    if (id_string.hasNext())	
                        instance_id = id_string.nextLine(); // 사용자가 입력한 값을 instance_id 변수에 저장   
		    // 입력받은 인스턴스 ID가 비어있지 않은지 확인
		    if (!instance_id.trim().isEmpty())
                        startInstance(instance_id); // 입력받은 유효한 인스턴스 ID를 사용하여 startInstance 메서드 호출
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
                case 14:
   		    listSecurityGroups();
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
	    // EC2 인스턴스 생성 요청을 구성
            RunInstancesRequest request = new RunInstancesRequest()
                    .withImageId("ami-0a661051b89baba13") // // 사용할 AMI ID를 설정
                    .withInstanceType(instanceType) // 생성할 인스턴스의 타입을 설정
                    .withMinCount(1) // 최소 인스턴스 수 설정
                    .withMaxCount(1) // 최대 인스턴스 수 설정
                    .withKeyName("cloud-test"); // 사용할 SSH 키 이름을 설정
		
	    // EC2 클라이언트를 사용하여 요청을 실행하고 응답을 받음
            RunInstancesResult response = ec2.runInstances(request);

	    // 성공적으로 생성된 인스턴스의 ID와 타입을 출력
            System.out.printf("Successfully created instance %s of type %s\n",
                    response.getReservation().getInstances().get(0).getInstanceId(), instanceType);
        } catch (AmazonServiceException ase) {
	    // Amazon EC2 서비스와 통신 중 발생한 예외 처리
            System.err.println("Service Exception: " + ase.getMessage());
        }
    }

	// EC2 인스턴스 목록을 출력하는 메서드
	public static void listInstances() {
        System.out.println("Listing instances...");
        boolean done = false; // 페이징된 응답 처리 여부를 확인하기 위한 변수
        try {
	    // DescribeInstances 요청 객체 생성
            DescribeInstancesRequest request = new DescribeInstancesRequest();

	    // 페이징 처리를 위한 루프	
            while (!done) {
		    
		// 요청을 실행하고 응답을 받음
                DescribeInstancesResult response = ec2.describeInstances(request);
		
		// 응답 내 각 Reservation 객체 처리
                for (Reservation reservation : response.getReservations()) {

		    // 각 Reservation 내의 Instance 객체 처리
                    for (Instance instance : reservation.getInstances()) {
			// 인스턴스의 상세 정보 출력
                        System.out.printf("[id] %s, [AMI] %s, [type] %s, [state] %10s, [monitoring state] %s\n",
                                instance.getInstanceId(), instance.getImageId(), instance.getInstanceType(),
                                instance.getState().getName(), instance.getMonitoring().getState());
                    }
                }

		// 다음 페이지 토큰을 설정
                request.setNextToken(response.getNextToken());

		// 다음 페이지가 없는 경우 루프 종료
                if (response.getNextToken() == null) {
                    done = true;
                }
            }
        } catch (AmazonServiceException ase) {
		
	    // AWS 서비스 호출 중 발생한 예외 처리
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

    // 가용 영역(Availability Zones)을 출력하는 메서드
    public static void availableRegions() {
        System.out.println("Available regions...");
        try {
		
	    // 가용 영역 정보를 요청하여 결과를 반환받음
            DescribeRegionsResult regions_response = ec2.describeRegions();

	    // 각 가용 영역 정보를 순회하며 출력
            for (Region region : regions_response.getRegions()) {
                System.out.printf("[Region] %s, [Endpoint] %s\n", region.getRegionName(), region.getEndpoint());
            }
        } catch (AmazonServiceException ase) {
		
	    // AWS 서비스 호출 중 발생한 예외 처리
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
    
    public static void listSecurityGroups() {
	    System.out.println("Listing security groups...");
	    try {
		DescribeSecurityGroupsResult result = ec2.describeSecurityGroups();
		for (SecurityGroup sg : result.getSecurityGroups()) {
		    System.out.printf("[Group ID] %s, [Group Name] %s, [Description] %s, [VPC ID] %s\n",
		            sg.getGroupId(), sg.getGroupName(), sg.getDescription(), sg.getVpcId());
		}
	    } catch (AmazonServiceException ase) {
		System.err.println("Service Exception: " + ase.getMessage());
	    }
    }

   
}

