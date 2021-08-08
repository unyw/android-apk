#!/bin/bash


echo "Creating $1.zip file with resized images..."
TMP_DIR=_temp_$1 # Set dir for temp files

# Make dirs
mkdir $TMP_DIR
mkdir "./$TMP_DIR/drawable-xxxhdpi/"
mkdir "./$TMP_DIR/drawable-xxhdpi/"
mkdir "./$TMP_DIR/drawable-xhdpi/"
mkdir "./$TMP_DIR/drawable-hdpi/"
mkdir "./$TMP_DIR/drawable-mdpi/"
mkdir "./$TMP_DIR/drawable-ldpi/"


# Resize image using ImageMagick
convert $1 -resize 100.0% "./$TMP_DIR/drawable-xxxhdpi/$1"
convert $1 -resize  75.0% "./$TMP_DIR/drawable-xxhdpi/$1"
convert $1 -resize  50.0% "./$TMP_DIR/drawable-xhdpi/$1"
convert $1 -resize  37.5% "./$TMP_DIR/drawable-hdpi/$1"
convert $1 -resize  25.0% "./$TMP_DIR/drawable-mdpi/$1"
convert $1 -resize  18.7% "./$TMP_DIR/drawable-ldpi/$1"

# Remove previous output and create zip
rm -f $1.zip
cd "./$TMP_DIR/"
zip ../$1.zip "./drawable-xxxhdpi/$1" "./drawable-xxhdpi/$1" "./drawable-xhdpi/$1" \
              "./drawable-hdpi/$1"    "./drawable-mdpi/$1"   "./drawable-ldpi/$1" 
cd ../

# Remove temp files
rm -fr $TMP_DIR

echo "Done!"
