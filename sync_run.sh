tch#!/usr/bin/env bash
############################################################
# Help                                                     #
############################################################
Help()
{
   # Display Help
   echo "Run an experiment on BigData1."
   echo
   echo "Syntax: sync_run [-h|p|b|r] -n CLASS"
   echo "options:"
   echo "n     Run the class specified by 'CLASS' ('Run' is default)."
   echo "h     Print this help."
   echo "p     Don't push source updates."
   echo "b     Don't re-build."
   echo "r     Don't pull results."
   echo
}

############################################################
############################################################
# Main program                                             #
############################################################
############################################################
############################################################
# Process the input options. Add options as needed.        #
############################################################
# Get the options
CLASS="Run"
export PUSH=1
export BUILD=1
export PULL=1
while getopts "n:hpbr" option; do
   case $option in
      h) # display Help
         Help
         exit;;
      n) # Enter a class
         CLASS=$OPTARG;;
      p) # Don't push
         PUSH=0;;
      b) # Don't build
         BUILD=0;;
      r) # Don't pull
         PULL=0;;
     \?) # Invalid option
         echo "Error: Invalid option"
         exit;;
   esac
done

export RESULT_DIR="/homes/ukumaras/Projects/fast/results_remote/"
export PROJECT_DIR="/homes/ukumaras/Projects/"

# Sync (push) files
if [ $PUSH == 1 ]; then
  if rsync -zarv --exclude 'target' --exclude 'results' --exclude '.git' --exclude '.idea' --exclude 'data' --exclude 'results_remote' ./ ukumaras@bigdata1.cs.purdue.edu:~/Projects/fast/;
  then
    echo "Push successful!"
  else
    echo "Error while running rsync!"
    exit
  fi
fi

# SSH in to the remote
# shellcheck disable=SC2087
ssh ukumaras@bigdata1.cs.purdue.edu << EOF

# Build and run
cd Projects/fast
if [ $BUILD == 1 ]; then
  mvn package -Dmaven.test.skip
fi

java -cp target/fastrelease-0.0.1-SNAPSHOT-jar-with-dependencies.jar edu.purdue.cs.fast.$CLASS $RESULT_DIR $PROJECT_DIR
EOF

if [ $PULL == 1 ]; then
  if rsync -zarv ukumaras@bigdata1.cs.purdue.edu:~/Projects/fast/results_remote/  ./results_remote/;
  then
    echo "Fetch results successful!"
  else
    echo "Error while running rsync!"
    exit
  fi
fi