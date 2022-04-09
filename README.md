# Encrypting Strings into Images: A Picture Tells x*(y-1) Characters
Project for COMP6841 Cyber Security 'Something Awesome' project by Damian Basso.

For this project, I have designed an encryption system built on writing encoded messages into images.
This consists of  an encryption function and its corresponding decryption function. 

## Why Encrypt Messages into Images

[Steganography](https://en.wikipedia.org/wiki/Steganography) is the practice of concealing a message within another message or medium of data. This can be done for a range of diffrent purposes, but the real reason this approach would be used is that it not only hides the content of the message, but can also obscure the message's existence entirely. Examples of steganography can be found throughout history and is a great technique to obscure sensitive information. These techniques often require some form of a private key or commonly shared secret among the exchanging parties that allows them to decode or extract the obscured message. 

So seeing as there is clear security value in obscuring one's message like this, the enormous amount of data within an image is a valuable target for hiding our own data. Images already store so much data, that an efficient cryptographic system should be able to store characters into the image with minimal distortion of that image, as to make it unnoticeable to the human eye. Photographs in particular are capable of generating and storing heaps of data which is unique to the particular photo since a photo is subject to a lot of variation from a range of factors i.e the equipment, the subjects, lighting, camera configuration and filters. Using modern computing power and some savvy maths encoding and decoding should be straightforward and effective, while remaining pretty much unnoticeable.

The practical use of such a system would be to transfer messages that only designated people who have access to the decryption algorithm are able to interpret. The main use case I've had in my head is that of a double agent. Such a person could broadcast large messages publicly and inconspicuosly by uploading encoded images to an Instagram account. Only the target of the communication who has the algorithn would be able to decode, and discretion could be maintained since the communication would not need to take a private channel and the contents is hidden.

## Measure of success

While I was  confident I would be able to successfully develop a basic model of this system, the main goals I aimed for and the two main 
ways in which I can measure the quality of my product are:

1) efficiency: how much data can be stored and how minimal can the distortion be  
2) security: how secure can this system be to prevent attacker's extracting the message without the decryption algorithm via brute force or otherwise.


## How it works + Maths

Images are ofcourse composed of many pixels. 
These pixels each have an assigned colour, which is represented in the RGBA format. This means that the colour has a red, green and blue value as well as 
an alpha (transparency) value, the combination of which results in a unique shade.
For this project, I have ignored the alpha channel as for most photographs and standard images, transparency does not occur, and hence manipulating the
alpha value would make the manipulation much more detectable.

Since we are using the standard UTF-8 character encoding, we need to represent 256 values. This is easily done by using mod(256) to limit the space of our values to only what we need.

Using this space, we need to ensure a formula whereby a pixel or a number of pixels and their 3 channels can be equated to a character that it is representing. To do this, I decided to make use of base 5 numerical representation, assigning each of the red, green and blue channels a weighting of 5^2, 5^1 and 5^0 respectively. When the values of the RGB channels are then summed together with these base 5 weightings, and then reduced to cover the mod 5 space. This gives us an integer < 256 which can be used as a character.

This gives us our formula f(R,G,B) = R\*5^2+G\*5^1+B\*5^0 (mod 256) where f is our encoded character.

For example to decode the pixel with RGB value RGB=(39,170,42) we simply plug in the values as done below.
  39\*5^2+170\*5^1+42\*5^0 (mod 256) = 79
  79 is O in UTF-8 so (39,170,42) represents 79
  
Using this representation system, we can easily mess with the RGB values to make them represent our desired character. Its worth noting that this system can be used individually with 1 pixel representing 1 character, or accross multiple pixels with all their channels being summed to give a character value. My algorithm does this to ensure efficiency, allowing it to use extra pixels to flatten the distortion if the images width\*height is >= message length * 2, meaning that there are anough pixels that 2 or more can be assigned to each character.

### Why base 5?

Since we are encoding over the mod(256) space, the most we will ever need to manipulate the value of a pixel's RGB value is 128, since we can make use of both adding and subtracting values. Hence, this is the important number When thinking about how I could interpret the different channels, the maths was focused on what would allow the representation to change while taking making the smallest impact on the actual channel values. The most efficient and straightforward way to do such a representation is to give each channel a different weighting. Base 5 I chose as it is the closest base to add up to 128 when limited to 3 digits i.e 503<sub>5</sub>==128 (not actual base 5, but an efficient representation). I intuitively thought that base 5 was the best bet since it was the closes to the 128 space when using 3 digits, but when I manually went through all the values up to 128 in an excel doc or base 4 and 6 it confirmed it.

Using base 5 means the most an individual pixel will ever need to change is by 12 values accross its 3 channels. This is when the pixel needs to be changed so its representation is 124 different. The value needs to be changed by 12 since 124 = 444<sub>5</sub>, so hence each of the channels needs to be changed by 4 values. This is the biggest change that is ever necessary.


### Other features and kinks

One other factor incorporated into the design is the use of the 'span' variable. Span is an integer which is how many pixels each character is represented over. So if span = 5, each character is represented by using the above base 5 system described, but summing together 5 consecutive pixels. Using this variable meant in order to decrypt an image, the span value would also have to be encrypted into the image so that the decrypter knows which pixels are meant to be connected to one another in the characters they represent. 

To make it easy and standardised, my algorithm encrypts the span value into the first row of the image. I did this by making it that when all the channels of all the pixels in the first row are summed together and weighted by their position along the x axis, mod the width of the image (x) it would equal span.

Using mod(width of image) is done because it gives span a range from 1 to x-1. The smaller span is, the larger the message is relative to the size of the image. Hence if span is the maximum value of x-1, message.length ~ y since span = x\*y/messagelength. Having a span value ~ x would mean encrypting a character every row of the image. I think these limits to span were acceptable since encoding a message with one character per line is very inefficient, and it would be easy enough to use a more suitably sized image or pad a message. 

The span value weights pixels by their position on the x axis because this makes it really easy to manipulate, since if the value the first row currently represents is n less than the value of span that we want it to represent, we can easily just add 1 to the nth pixel, and hence span is correctly represented with 1 pixel change.

The alternative to encoding span into the image, would be to directly encode in the message length which is something I avoided as that number is more subject to vary and generally will be much larger and harder to represent over a fixed space. The downside to not doing this, is the decrypting function doesn't know the messages actual length, so if the message length doesn't perfectly divide the image, there will be excess pixels that the algorithm doesn't store any data in and are read as random values on the end of the message the decrypter outputs. This is a kink that I didn't fix as I think its pretty insignificant and doing something to remedy it would result in distortion or inefficiency.


## Possible Improvements 

There's quite a bit that I had on the agenda to add to this project that I couldn't get around to before the submission date. The easiest one would be to add a cipher to the message just as an extra layer in case someone attempts to brute force the image to try and pull characters from it. This would be really easy to due since its all done via running a program, and hence private keys can be used. I didn't do this as the more interesting and important factor on the assignment was actually getting the encoding into the image efficient and impressive, and while a cipher would be a good addition, it would be simple and not contribute much to the overall project, so I prefered to focus on designing the overall system more.

I think a big improvement could also be made in how I store span into the encrypted image. Placing it in the first row of the image I planned to be a placeholder. I think it would be much better to find another technique to put it in, possibly distributing the value, hiding it in pixels accross the image. The decrypter would then know where these values are based on a particular pattern it uses to hide them, possibly based on the images height and/or width since these are supplied integers in the image.

The biggest improvement I was hoping to get in was a method by which the message would not be encrypted linearly across the iamge, but rather distributed in different places by using primitive roots. Primitive roots are a key aspect of modular arithmetic, and they would be useful as using a primitive root means we can encode into the entire of the image without redundancy while avoiding linearity. The problem I ran into was mostly just from this being too complex as the only numbers which have primitive roots are prime numbers, powers of primes and 2\*primes. My idea was to try and find the largest of these values that have primitive roots that is less then the total size of the image, and then find the primitive root and use it to determine the order by which pixels are encoded into the image. I still think this probably would work, but I ran out of time doing the math schematics so I couldn't get to implementing
