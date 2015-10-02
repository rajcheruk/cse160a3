CS 160 Homework Assignment #3
Image Segmentation TO BE DONE IN TEAMS OF TWO
Due: Friday 2/19 at 6PM

Change log
Date 	Description
05-Feb-10 	Original posting

Introduction

In this assignment you’ll parallelize an image segmentation code written in Java, perform optimizations to improve scalability and single processor performance, and characterize performance. You will use Java Threads rather than PJ. You may develop your code wherever you wish, but grading will be based on behavior and performance observed on ieng6-203. So be sure to run on that machine before you turn in the assignment.
Software and documentation

The code is found in two places. In ~cs160w/../public/HW/A3 and in the tar file A3.tgz. We have set up images for testing in ~cs160w/../public/HW/A3/images, and there is also a subdirectory of A3, called makeBoxes with programs to create various simple "phantom" images containing boxes, that will help you debug your code. Some images were created from these programs and installed for you in the images directory, but you may want to experiment with the program.s

The "bear" image Bear\_2000\_adj\_Eq\_1.png is a 1477 × 2000 image and it also has smaller versions. Use the file (1) command to report the image sizes.

You may choose to use your own images. For example, the following web sites provide downloadable images:
# http://sampl.ece.ohio-state.edu/data/stills/sidba/
# http://sipi.usc.edu/database/
# http://www1.cs.columbia.edu/CAVE/software/curet/

However, most images are noisy and you may need a photo editor like Gimp or Photoshop to smooth the image (say with a Gaussian Blurring filter), increase the contrast or both.
Documentation
# Java Documentation (1.5)
# Writing multithreaded Java applications, Alex Roetter, IBM Developerworks
# Javasoft Thread Tutorial, Concurrency
# http://java.sun.com/docs/books/tutorial/essential/concurrency/index.html
# Working with Images, The Java Tutorials
The Segmentation code

Class SegmentImg implements a serial image segmentation program for black and white images. As in the previous assignment, we will use BufferedImage and imageIO to transfer images to and from disk and memory, and ordinary Java 1D int arrays when working with images in memory. The imageIO API supports jpeg and various other formats, but not tiff (We’ll provide you with png and jpeg images, but if you are curious about using tiff, you’ll need to investigate further). To see the full list of supported formats from imageIO, compile and run SupportedFormats.java.

The images are read into a BufferedImage for easy manipulation of the underlying WritableRaster.

The serial implementation is provided in SegmentImg.java which is the driver program, ImageUtils.java, which provides some utilities for handling images and Segmentation.java which does the segmentation.
Running the code
Command line options are available to shut off the display, which comes in handy when assessing performance. You may experience delays in displaying the picture if you are working remotely. The options are as follows
-i 	input-image
-t 	turns off display, reports compute time only
-f 

&lt;format&gt;

 	use format as output format (default png)
-s 

&lt;n&gt;

 	threshold value (default 4)
-R 

&lt;n&gt;

 	use 

&lt;n&gt;

 as seed to the random number generator which in turn chooses color on labels
-h 	Holds the display when the program finishes.
-p 

&lt;n&gt;

 	run in parallel mode n threads [used for the serial code](Not.md). If n=1, run the parallel version on one thread.

SegmentImg uses default input and output images names: input.png and output.png. You could also achieve the same effect with

java SegmentImg -i input.png -t -s 2

The above invocation will not produce a display, and will consider neighboring pixels to be in the same cluster if they differ by not more than 2 quantum levels. Before the program terminates, it saves the output image into a file with name inputfilename-out\_sm.fileformat. (Ex: input-out\_sm.png).

If your program runs out of memory, use the -Xmx flag to increase the maximum size of the allocation pool. For example, -Xmx1024m increases the maximum size to 1024 MB. (See the java man page for more details)
Parallelization
The segmentation algorithm we use in this project has log2(N) phases if the input image size is N × N. The provided serial algorithm has multiple phases, with two passes in each phase.

In the parallel implementation, first, divide the images into stripes, and perform serial segmentation on each image stripe in parallel with the others. The result will be that each stripe will label its clusters using local labels. You can either create local label array per thread or use a global label array shared by all the threads. Discuss the advantages and disadvantages of choice in the report.

After all the phases are complete, neighboring pixels residing in different stripes may or may not have the same local label. The next step resolves these differences, ensuring that neighbors in the same cluster have the same global label. You can implement this step serially, but be sure to measure and discuss the effect of these serial bottlenecks.

Once you have this basic implementation running, perform a scaling analysis, running a fixed sized program on 1, 2, 4, 6 and 8 threads. Where are the bottlenecks? Optimize performance as best you can and repeat the analysis.

You can create a new class with name ParallelSegmentation that extends Thread class and implement the multithreaded segmentation in that class. You can add necessary changes in the SegmentImg class to fork/join threads or create a separate class to handle master thread tasks.

Note that the serial segmentation continuously displays the segmented image. We do not ask you to integrate the display with the parallel implementation. Therefore, you can comment out the following lines in your parallel version:

> // comment it out for parallel version
> > gui.updateTimer(pp);


> // comment it out for parallel version
> > gui.updateSegmentedImage(getSegmentedImage(), false);


> // comment it out for parallel version
> > gui.updateDoneLabel();

Extra Credit - Continuous Display (10 points)
Integrate the display with your parallel implementation. Only one of the threads should perform the display, others should wait. After each phase, you need a barrier to synchronize the threads and gather the labels from all the threads to have a complete image.
Extra Credit -Early Termination (5 points)
There are log2(N) phases but most of the time, the segmentation converges before the last phase. Improve the code so that the segmentation terminates if there are no more changes in the labels.
How to test your code
You will need to test code correctness as well as performance,. Next week we will post performance that will help you determine when performance is reasonable.
Correctness
The provided code saves the segmented image into a file. Besides testing visually, you should test the correctness of your code by using diff Unix command to compare two images as follows:

% diff image1.png image2.png

If the images are different, the command returns:

Binary files image1.png and image2.png differ

NOTE: If a display window does not appear on your screen, then your SSH display settings are not set correctly. If you are using a Linux based machine and are connecting to a Linux machine using SSH, use the -X option:
% ssh -X yourlogin@machine.ucsd.edu

If you are using a Windows based machine, and are using a 'secure shell client' AND 'exceed' (or xwin32, or cygwin) to log into Linux machine, you must ensure that 'X11 tunneling' is set. [you have problems with this, first read our webboard on this issue. If you still have the problem, post to the A3 Noodle Forum ](If.md)

We have provided a set of images in $pub/HW/A3/images. Use smaller images when developing your code.
Performance
There are no performance goals for your parallel implementation. Please feel free to post your performance data on the A3 Moodle forum to encourage a friendly competitive spirit. We will announce the best parallel implementations in terms of performance and code quality.
Experimentation and Report

Using your parallel and the provided serial implementation, report the parallelization overhead on 1 thread and parallel speedup on 2, 4, 6 and 8 threads for your initial and final optimized implementations.

Take the timings with display mode off.

Use the elapsed time printed to stdout.

Plot the speedup curve, and provide a table showing both running time and speedup. Also plot the efficiency.

Are you satisfied with the speedups for your initial implementation? Why or why not? Explain your results thoroughly conducting any experiments needed to help you explain your results. If you aren’t sure, make an educated guess.
Things you should turn in

Document your work in a well-written report which discusses your findings and design decisions carefully. Your writeup should describe how you parallelized the program to run in multithreaded mode, how you optimized it, and how you determined that the program is correct, that is, produces results that are consistent with the serial code. Use the provided images to help you validate the multithreaded implementation. We will use private tests that will not be posted. Use pseudocode as necessary to explain fixes or code structures.

Discuss any factor(s) you think may have limited performance, especially if you were not able to meet the performance goal (to be posted next week), and if you think you could improve performance further. In case of further improvements, be sure to describe them in sufficient detail to enable the reader to decide whether there would be a benefit to those improvements.

Your report should cite any written works or software as appropriate in the text of the report. You may not use code you didn’t write. If there are any questions about this, please see the Instructor.

Provide a hard copy of your writeup by the due date. Also transmit the report electronically along with your code and any output needed to verify correctness. Be sure and delete all .class files and executables before creating the archive file. Transmit a single archive file per team, e.g. .zip, .tgz, .jar, containing your writeup, and source code, plus any output files. Email the archive file as an attachment to the following address:
cs160w1 	@ 	ieng6.

> ucsd.
> edu
Be sure to put the string cse160:A3 in the subject line and to use the email address above; not doing so may result in a penalty.

The name of your tar file should start with your login IDs separated by underscore, e.g. partner1\_partner2.tgz. Your archive file should create a directory with the name of your login IDs. The directory should contain two subdirectories

src:     All source code. Do not change the name of any provided source code modules, nor introduce any new ones.
report: Your report including any separate image files, screen shots, or other output.
At the top level of your directory there should be an ASCII file called README.txt, that lists all the parts of your electronic submission. If you like, include an html file called index.html at the top level. From there you can then link to a README file as well as the src and report directories.


Copyright © 2010 Scott B. Baden Fri   [5 20:51:45 PST 2010](Feb.md)