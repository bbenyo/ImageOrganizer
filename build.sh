#!/bin/bash

gradle fatjar
cp build/libs/*.jar .
