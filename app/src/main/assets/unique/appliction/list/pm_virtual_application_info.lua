function before(hook, param)
    local packageName = param:getArgument(0)
    local fake = param:getVirtualApplicationInfo(packageName)
    if fake == nil then
        return false
    end
    param:setResult(fake)
    return true
end

function after(hook, param)
    local packageName = param:getArgument(0)
    if param:getVirtualApplicationInfo(packageName) ~= nil then
        return false
    end
    local result = param:getResult()
    if result == nil or param:isPackageAllowed(packageName) then
        return false
    end
    local cls = luajava.bindClass("android.content.pm.PackageManager$NameNotFoundException")
    param:setResult(luajava.new(cls, packageName))
    return true
end
