#! /bin/bash

ls -a | grep -v rundocker.sh | grep -v generate.sh | grep -v clean.sh  | grep -v '^\.' | grep -v 'READ.ME' | xargs rm && rm *~ 


