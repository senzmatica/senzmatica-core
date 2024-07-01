
Name:           magma-core
Version:        1.0.0.4
Release:        1%{?dist}
Summary:        core service of magma platform

License:        GPL
BuildRequires:  java-devel
BuildRequires:  maven
Requires:       java
Requires:       systemd
URL:            https://Tharmika_R@bitbucket.org/magma-senz/magma-core.git
Source0:        %{name}-%{version}.tar.gz
BuildArch:      noarch

%description
This is a custom RPM for magma-core Spring Boot Java application.

%define __jar_repack %{nil}

%prep
%setup -q -n magma-core/magma-service


%build
mvn compile
mvn package -DskipTests

%install
mkdir -p %{buildroot}/opt/magma-core/
# Debugging: Print contents of service/target directory
ls -l /home/azureuser/rpmbuild/BUILD/magma-core/magma-service/target/
#Copy only the necessary files and directories
cp -r /home/azureuser/rpmbuild/BUILD/magma-core/magma-service/target/magma-core.jar %{buildroot}/opt/magma-core/


%files
%defattr(-,root,root,-)
/opt/magma-core


%post
#!/bin/bash

# Create the service file
cat > /etc/systemd/system/magma-core.service << EOF
[Unit]
Description=Magma core Service
After=network.target


[Service]
Type=simple
WorkingDirectory=/opt/magma-core
ExecStart=/usr/bin/java -jar /opt/magma-core/magma-core.jar

[Install]
WantedBy=multi-user.target
EOF

# Enable the service
systemctl enable magma-core.service


%preun
if [ $1 -eq 0 ] ; then
    # This means it's an uninstallation
    systemctl disable magma-core.service
    systemctl stop magma-core.service
    rm -f /etc/systemd/system/magma-core.service
fi
