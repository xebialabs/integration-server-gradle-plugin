#!/bin/bash

current_ids=`ps -aef | grep SatelliteBootstrappe | grep -v grep | awk '{print $2}'`

for id in $current_ids; do
  kill -9 $id
done