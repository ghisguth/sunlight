precision mediump float;

uniform float blur;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;

void main() {
  float NUM_SAMPLES = 8.0;
  float density = 0.1;
  float exposure = 1.0 / NUM_SAMPLES;
  float decay = 0.95;
  float weight = 0.6;

  // Calculate vector from pixel to light source in screen space.
  vec2 deltaTexCoord = vTextureCoord - vec2(0.5, 0.5);

  // Divide by number of samples and scale by control factor.
  deltaTexCoord *= 1.0 / NUM_SAMPLES * density;

  // Store initial sample.
  vec3 color = texture2D(sTexture, vTextureCoord).xyz;

  vec3 colorInit = color;

  // Set up illumination decay factor.
  float illuminationDecay = 1.0;

  vec2 texCoord = vTextureCoord;

  // Evaluate summation from Equation 3 NUM_SAMPLES iterations.
  for (float i = 0.0; i < NUM_SAMPLES; i++)
  {
    // Step sample location along ray.
    texCoord -= deltaTexCoord;
    // Retrieve sample at new location.
    vec3 sample = texture2D(sTexture, texCoord).xyz;

    // Apply sample attenuation scale/decay factors.
    sample *= illuminationDecay * weight;

    // Accumulate combined color.
    color += sample;

    // Update exponential decay factor.
    illuminationDecay *= decay;
  }

  // Output final color with a further scale control factor.
  gl_FragColor = vec4( colorInit*0.4 + color * exposure, 1.0);
}
