require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = package['name']
  s.version      = package['version']
  s.summary      = package['description']
  s.license      = package['license']

  s.authors      = package['author']
  s.homepage     = package['homepage']
  s.platforms    = { :ios => "9.0", :osx => "10.13" }

  s.source       = { :git => "https://github.com/react-native-webview/react-native-webview.git", :tag => "v#{s.version}" }
  s.source_files  = "apple/**/*.{h,m}"

  s.dependency 'React-Core'

  if defined?($RNWeb2MobileAsStaticFramework)
    Pod::UI.puts "#{s.name}: Using overridden static_framework value of '#{$RNWeb2MobileAsStaticFramework}'"
    s.static_framework = $RNWeb2MobileAsStaticFramework
  else
    s.static_framework = false
  end
end
